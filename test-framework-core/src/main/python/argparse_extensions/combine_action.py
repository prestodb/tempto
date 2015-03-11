# Copyright Teradata Inc., 2015"

import argparse

class CombineAction(argparse.Action):
    """
    This action combines multiple LIST arguments into one, by joining all of them
    """
    def __call__(self, parser, namespace, values, option_string=None):
        if hasattr(namespace, self.dest):
            old_values = getattr(namespace, self.dest)
            if old_values is not None:
                values = values + old_values
        setattr(namespace, self.dest, values)