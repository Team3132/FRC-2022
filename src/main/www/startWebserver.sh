#!/bin/bash
# Run this in the top level directory of the USB flash drive.

cat <<EOF

Access logs and charts with your web browser:

  http://localhost:8000/

Starting webserver. Use Control+C to exit.

EOF

# cd to the same directory as this file so that the
# webserver serves this directory, not the directory
# that the user ran this script from.
cd $(dirname $0)

python3 -m http.server
