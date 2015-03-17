#!/usr/bin/python

import argparse
import ctypes
import math
import struct
import subprocess
import sys

# Note: To run the unit tests via doctest, replace the main() call with:
#    import doctest
#    doctest.testmod()


# The epsilon used for comparison was found at:
# http://en.wikipedia.org/wiki/Floating_point#Minimizing_the_effect_of_accuracy_problems
MAX_ABS_DIFF = 1e-13

# This is what Google's googletest C++ Testing Framework uses.
# http://code.google.com/p/googletest/wiki/AdvancedGuide#Floating-Point_Comparison
MAX_ULPS_DIFF = 4

# abs(actual - expected) / expected
# This value was chosen semi-arbitrarily, we need to determine this threshold more rigorously.  See HADAPT-3147.
MAX_RELATIVE_ERROR = 1e-10

class FloatingPoint(object):
    @staticmethod
    def _parse_as_type(value_str, format):
        """
          Parse the given string as a floating point of the given type, "f" for
          a 32-bit single-precision value (float) and "d" for a 64-bit
          double-precision value (double).
          
          ### Try special values
          >>> FloatingPoint._parse_as_type("NaN", "f")
          nan
          >>> FloatingPoint._parse_as_type("NaN", "d")
          nan

          >>> FloatingPoint._parse_as_type("Infinity", "f") 
          inf
          >>> FloatingPoint._parse_as_type("Infinity", "d")
          inf

          ### Try a safe value, with a binary-representable fractional part
          >>> FloatingPoint._parse_as_type("1.25", "f")
          1.25
          >>> FloatingPoint._parse_as_type("1.25", "d")
          1.25

          ### Try some values that seem safe as floats but aren't
          >>> FloatingPoint._parse_as_type("1.2", "f")
          Traceback (most recent call last):
          ...
          ValueError: floating point "1.2" can't be represented as a "f" float; got: 1.20000004768
          
          >>> FloatingPoint._parse_as_type("1.2", "d")
          1.2

          >>> FloatingPoint._parse_as_type("1000664.7437", "f")
          Traceback (most recent call last):
          ...
          ValueError: floating point "1000664.7437" can't be represented as a "f" float; got: 1000664.75

          >>> FloatingPoint._parse_as_type("1000664.7437", "d")
          1000664.7437

          ### Try a value that overflows both float and double (derived from sys.float_info.max)
          >>> FloatingPoint._parse_as_type("1.7976931348623157e+309", "f")
          inf
          >>> FloatingPoint._parse_as_type("1.7976931348623157e+309", "d")
          inf
        """
        # parse into a python float, which is always 64-bit    
        as_py_float = float(value_str)
    
        # convert it to a c type to get the specified precision
        if (format == "f"):
            as_c_type = ctypes.c_float(as_py_float)
        elif (format == "d"):
            as_c_type = ctypes.c_double(as_py_float)
        else:
            raise ValueError("unknown format: %s" % format)
       
        # make sure it casted cleanly (without truncation or overflow/underflow)
        if (not math.isnan(as_py_float)) and (as_c_type.value != as_py_float):
            raise ValueError(("floating point \"%s\" can't be represented " 
                                 "as a \"%s\" float; got: %s") 
                                % (value_str, format, as_c_type.value))
            
        return as_c_type.value
  
    @staticmethod
    def _get_raw_bits(value, format):
        """
          Extract the raw bits of the floating point value as an integer large 
          enough to hold it, "f" for a 32-bit single-precision value (float) and
          "d" for a 64-bit double-precision value (double).
          
          ### Try special values
          ### NaN = 0x7fc00000, 0x7ff8000000000000
          >>> FloatingPoint._get_raw_bits(float("NaN"), "f")
          2143289344
          >>> FloatingPoint._get_raw_bits(float("NaN"), "d")
          9221120237041090560

          ### Infinity = 0x7f800000, 0x7ff0000000000000
          >>> FloatingPoint._get_raw_bits(float("Infinity"), "f") 
          2139095040
          >>> FloatingPoint._get_raw_bits(float("Infinity"), "d")
          9218868437227405312

          ### Try a safe value
          >>> FloatingPoint._get_raw_bits(float("1.25"), "f")
          1067450368
          >>> FloatingPoint._get_raw_bits(float("1.25"), "d")
          4608308318706860032
          
          ### Try a value which will get truncated as a float
          >>> FloatingPoint._get_raw_bits(float("1000664.7437"), "f")
          1232358796
          >>> FloatingPoint._get_raw_bits(float("1000664.7437"), "d")
          4696842856789589780
        """
        if (format == "f"):
            bits_format = "i"
        elif (format == "d"):
            bits_format = "q"
        else:
            raise ValueError("unknown format: %s" % format)

        # pack as a big-endian floating point then unpack as an integer to get the raw bits
        packed = struct.pack(">" + format, value)
        return struct.unpack(">" + bits_format, packed)[0]
    
    def __init__(self, value_str, format, sign_bitcount, exponent_bitcount, fraction_bitcount):
        self.sign_bitcount = sign_bitcount
        self.exponent_bitcount = exponent_bitcount
        self.fraction_bitcount = fraction_bitcount
      
        self.sign_bitmask = (~0) << (self.exponent_bitcount + self.fraction_bitcount)
        self.exponent_bitmask = ((~0) << self.fraction_bitcount) & ~self.sign_bitmask
        self.fraction_bitmask = ~(self.sign_bitmask | self.exponent_bitmask)

        self.value = FloatingPoint._parse_as_type(value_str, format)
        self.bits = FloatingPoint._get_raw_bits(self.value, format)
    
    def sign_bit(self):
        return (self.bits & self.sign_bitmask) >> (self.exponent_bitcount + self.fraction_bitcount)
    
    def exponent_bits(self):
        return (self.bits & self.exponent_bitmask) >> self.fraction_bitmask

    def fraction_bits(self):
        return self.bits & self.fraction_bitmask
    
    def is_nan(self):
        return (((self.bits & self.exponent_bitmask) == self.exponent_bitmask) 
                and (self.fraction_bits() != 0))
    
    # derived from:
    # http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
    def almost_equals(self, that, max_abs_diff=MAX_ABS_DIFF, max_ulps_diff=MAX_ULPS_DIFF, max_relative_error=MAX_RELATIVE_ERROR):
        abs_diff = abs(self.value - that.value)
        if (abs_diff <= max_abs_diff):
            return True

        if (self.sign_bit() != that.sign_bit()):
            return False

        ulps_diff = abs(self.bits - that.bits)
        if (ulps_diff <= max_ulps_diff):
            return True

        if that.value != 0:
            relative_error = abs_diff / that.value
        else:
            relative_error = abs_diff
        return (relative_error < max_relative_error)

class Float(FloatingPoint):
    # sign bitmask      0x 8000 0000
    # exponent bitmask  0x 7F80 0000
    # fraction bitmask  0x 007F FFFF
    def __init__(self, value_str):
        super(Float, self).__init__(value_str, "f", 1, 8, 23)

class Double(FloatingPoint):
    # sign bitmask      0x 8000 0000 0000 0000
    # exponent bitmask  0x 7FF0 0000 0000 0000
    # fraction bitmask  0x 000F FFFF FFFF FFFF
    def __init__(self, value_str):
        super(Double, self).__init__(value_str, "d", 1, 11, 52)

def diff_files(filename_left, filename_right, token_delimiter):
    """
      Diff two files line-by-line, and return a dictionary of mismatches. The 
      dictionary maps the line number of the mismatch to a list of the two 
      mismatched lines, eg: "{ line_number : [line_from_left, line_from_right] }"
      
      :return: a dictionary of mismatches
    """
    file_left = open(filename_left, "r")
    file_right = open(filename_right, "r")
    
    line_number = 0
    mismatches = {}
    for line_left in file_left:
        line_number += 1
      
        line_right = file_right.readline()
        if not line_right:
            mismatches[line_number] = [line_left, "EOF\n"]
            break
        
        current_lines_match = compare_lines(line_left, line_right, token_delimiter)
        if not current_lines_match:
            mismatches[line_number] = [line_left, line_right]
       
    line_right = file_right.readline()                
    if file_right.readline():
        mismatches[line_number] = ["EOF\n", line_right]
        
    return mismatches

def compare_lines(line_left, line_right, delimiter):
    """
      Returns True if the two lines are identical; False otherwise. This method
      will tokenize the two lines using the specified delimiter, and then 
      compare the resulting tokens. 
      
      :return: True if the two lines are identical; False otherwise
    """
    tokens_left = line_left.split(delimiter)
    tokens_right = line_right.split(delimiter)
    if len(tokens_left) != len(tokens_right):
        return False

    for idx in range(len(tokens_left)):
        if not compare_tokens(tokens_left[idx], tokens_right[idx]):
            return False
          
    return True

def compare_tokens(token_left, token_right):
    """
      Returns True if the two tokens are identical, or equal within some 
      tolerance; False otherwise.  This method will attempt to parse the tokens
      as floats, and if successful, compare the float values using 
      Float.almost_equals; otherwise, the tokens are compared as strings.
      
      :return: True if the two tokens are identical, or equal within some 
               tolerance; False otherwise
    """
    try:
        float_left = Float(token_left)
        float_right = Float(token_right) 

        if float_left.is_nan() and float_right.is_nan(): 
            return True
            
        return float_left.almost_equals(float_right)
      
    except ValueError as e:
        pass
      
    try:
        double_left = Double(token_left)
        double_right = Double(token_right) 

        if double_left.is_nan() and double_right.is_nan(): 
            return True
            
        return double_left.almost_equals(double_right)
  
    except ValueError as e:
        pass
      
    return (token_left == token_right)


parser = argparse.ArgumentParser(description=("A simple file differencing tool which diffs two files line by line, "
                                              "and token by token. If tokens represent numerical values, they are "
                                              "parsed into 32-bit floats and compared using a scheme that allows for "
                                              "minor deltas, which accounts for the inherent inaccuracy in floating "
                                              "point representations. If any mismatches are found, they are printed "
                                              "to stderr, and the output of a regular diff is printed to stdout."))
parser.add_argument("file_1", 
                    help="One of the files to diff. Lines from this file will be prepended with '<'.")
parser.add_argument("file_2", 
                    help="The other file to diff. Lines from this file will be prepended with '>'.")
parser.add_argument("-d", "--token_delimiter", action="store", default="|",
                    help="The token delimiter. Lines will be tokenized using this character. Default: \"|\"")
parser.add_argument("--no_diff", action='store_true', default='false', help="Show output from fpdiff only.  Do not run Unix diff.")

def main():
    args = parser.parse_args()

    mismatches = diff_files(args.file_1, args.file_2, args.token_delimiter)

    if args.no_diff == 'false' and len(mismatches) != 0:
        print >>sys.stderr, "--------------------------------------------------------------------------------"
        print >>sys.stderr, "output of \"fpdiff.py\" -- " 
        print >>sys.stderr 

    for mismatched_line_number in sorted(mismatches.iterkeys()):
        mismatched_line_left = mismatches[mismatched_line_number][0]
        mismatched_line_right = mismatches[mismatched_line_number][1]

        print >>sys.stderr, ("%7d < %s        > %s" 
                             % (mismatched_line_number, mismatched_line_left, mismatched_line_right))

    # TODO once this script runs on top of diff's output, we should only output our results.
    #      we're including diff's output in case rows are missing/added, to facilitate debugging.

    if args.no_diff == 'false' and len(mismatches) != 0:
        # flush stderr to make sure stderr and stdout don't get interleaved
        sys.stderr.flush()

        print "--------------------------------------------------------------------------------"
        print "output of \"diff\" -- " 
        print 

        # flush stdout to make sure diff's output is below what we just printed
        sys.stdout.flush()

        subprocess.call(["diff", args.file_1, args.file_2])

    sys.exit(len(mismatches) != 0)

if __name__ == "__main__":
    main()
