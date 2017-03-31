# jOSPIirrigation
OSPI Java app to customize station activation.  The app initially creates a database name ospidata.db.  This SQLite database contains four tables as follows:

## info table 
which contains four fields as follows:
 password  -  This contains either a plaintext password or a md5hashed password.  The type of password depends on the value in the md5hash field.  If the md5hash field contains a numeric ZERO, the password is a plaintext password.  If the field contains a numeric ONE, the password is already md5hashed.
md5hash  -  Contains numeric ZERO for plaintext password or numeric ONE for md5hashed password.
urlport  -  the url of the Open Sprinkler Raspberry Pi.  Format example is http://192.168.1.101
port  -  The port that is used by the Open Sprinkler Raspberry Pi.

## zones table 
which contains two fields as follows:
 idnum  -  The station id number
lastwatered  -  Thedate and time the station id was last activated

## log table 
which contains two fields as follows:
 timestamp  -  Date and time of entry
info  -  relevant info posted to log such as successfully activated a watering station etc.

## programs table 
which contains three fields as follows:
 name  -  Self explanatory, the name of the program
zoneseq  -  The order in which the stations (zones) are to be watered and how long each station (zone) is to be activated. For example,  zone 3 should be watered 180 seconds,  3:180.  There is a colon between the station (zone) number and number of seconds the station (zone) should be activated.  Please note that there is a comma separating each station (zone). For example, 1:30,2:45,9:30,3:180,11:15,4:90  Please accurately follow this format to ensure that the program functions properly. Also, the station (zones) do NOT have to be in sequence.  You can use any order of stations to meet you custom irrigation needs.
last executed  -  Date and time the program was last activated (executed).

To execute the program from the command line: jOSPIirrigation Program Name
Examples:
jOSPIirrigation Program1
If there are any spaces in the name of your program, you must enclose the program name in quotes as follows:
jOSPIirrigation "Program Test"

The program will create the ospidata.db if it does NOT exist.  It will place a lot of default values in the various table fields.  You can use a graphical database editor such as DB Browser for SQLite to edit these fields. By default, the ospidata.db is created in the Windows c:/temp folder or the Linux /usr/local folder.  So, the very first time you execute the app, it will probably fail since the default values such as url, password etc are not correct.  Use a DB editor to update values and rerun the program.
