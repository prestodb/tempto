#
# Copyright Teradata, Inc. 2015
#


def comma_separated_list(value):
    return value.split(',')


def lower_case_list(l):
    return [value.lower() for value in l]


def ensure_arg_is_list(value):
    if isinstance(value, str):
        return [value]
    else:
        return value