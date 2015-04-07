# Copyright Teradata Inc., 2015"

import argparse


class CombineAction(argparse.Action):

    def __init__(self, **kwargs):
        self.__default = kwargs['default'] if 'default' in kwargs else None
        super(CombineAction, self).__init__(**kwargs)

    """
    This action combines multiple LIST arguments into one, by joining all of them
    """
    def __call__(self, parser, namespace, values, option_string=None):
        if hasattr(namespace, self.dest):
            old_values = getattr(namespace, self.dest)
            if old_values is not self.__default:
                values = values + old_values
        setattr(namespace, self.dest, values)
