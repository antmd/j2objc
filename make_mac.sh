#!/bin/bash - 
#===============================================================================
#
#          FILE: make_mac.sh
# 
#   DESCRIPTION:  
# 
#       CREATED: 2014-01-19
#
#        AUTHOR: Anthony Dervish
#
#===============================================================================

set -o nounset                              # Treat unset variables as an error

make J2OBJC_ARCHS=macosx DEBUGGING_SYMBOLS=YES
