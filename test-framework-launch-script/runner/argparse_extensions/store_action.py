# Copyright Teradata Inc., 2015"

import argparse


class StoreAction(argparse.Action):

    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, values)
