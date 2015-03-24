# Copyright Teradata Inc., 2015"

import argparse

class StoreAction(argparse.Action):

    def __init__(self, **kwargs):
        super(StoreAction, self).__init__(**kwargs)

    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, values)
