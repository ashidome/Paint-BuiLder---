#!/bin/sh
# AUTO-GENERATED FILE, DO NOT EDIT!
if [ -f $1.org ]; then
  sed -e 's!^C:/cygwin/lib!/usr/lib!ig;s! C:/cygwin/lib! /usr/lib!ig;s!^C:/cygwin/bin!/usr/bin!ig;s! C:/cygwin/bin! /usr/bin!ig;s!^C:/cygwin/!/!ig;s! C:/cygwin/! /!ig;s!^N:!/cygdrive/n!ig;s! N:! /cygdrive/n!ig;s!^M:!/cygdrive/m!ig;s! M:! /cygdrive/m!ig;s!^L:!/cygdrive/l!ig;s! L:! /cygdrive/l!ig;s!^I:!/cygdrive/i!ig;s! I:! /cygdrive/i!ig;s!^D:!/cygdrive/d!ig;s! D:! /cygdrive/d!ig;s!^C:!/cygdrive/c!ig;s! C:! /cygdrive/c!ig;' $1.org > $1 && rm -f $1.org
fi
