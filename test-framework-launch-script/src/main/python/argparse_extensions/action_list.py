# Copyright Teradata Inc., 2015"

import argparse

def action_list(action_classes):
    class ActionList(argparse.Action):
        def __init__(self, **kwargs):
            self.__actions = [action_class(**kwargs) for action_class in action_classes]
            super(ActionList, self).__init__(**kwargs)

        """
        This action combines multiple actions that are executed sequentially
        """
        def __call__(self, *args):
            for action in self.__actions:
                action(*args)
    return ActionList
