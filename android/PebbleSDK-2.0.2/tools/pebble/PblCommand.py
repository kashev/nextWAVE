import os

class PblCommand:
    name = ''
    help = ''

    def run(args):
        pass

    def configure_subparser(self, parser):
        parser.add_argument('--sdk', help='Path to Pebble SDK (ie: ~/pebble-dev/PebbleSDK-2.X/)')
        parser.add_argument('--debug', action='store_true', 
                help = 'Enable debugging output')

    def sdk_path(self, args):
        """
        Tries to guess the location of the Pebble SDK
        """

        if args.sdk:
            return args.sdk
        else:
            return os.path.normpath(os.path.join(os.path.dirname(__file__), '..', '..'))
