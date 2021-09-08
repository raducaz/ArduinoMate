adb kill-server
adb start-server
adb usb
adb devices



$ adb shell
$ su
# mount -o rw,remount /system
adb remount
adb push android.hardware.usb.host.xml /system/etc/permissions
adb reboot

How to create a file in Linux from terminal window?
Create an empty text file named foo.txt:
touch foo.bar
OR
> foo.bar
Make a text file on Linux:
cat > filename.txt
Add data and press CTRL+D to save the filename.txt when using cat on Linux
Run shell command:
echo 'This is a test' > data.txt
Append text to existing file in Linux:
echo 'yet another line' >> data.txt

How to enable USB host API support
emetemunoy edited this page on Jun 5, 2015 · 2 revisions
#How to enable USB host API support

Some Android devices have no support USB On The Go (OTG)
USB host mode is supported in Android 3.1 and higher
Also for this you need ROOT access.
To enable USB host API support you should add a file named android.hardware.usb.host.xml and containing the following lines:

<permissions>
 <feature name="android.hardware.usb.host"/>
</permissions>
into folder

/system/etc/permissions
in that folder find file named

handheld_core_hardware.xml or tablet_core_hardware.xml

and add

<feature name="android.hardware.usb.host" />
into <permissions> section.

Reboot your device. USB host API should work.

Procedure:

adb pull /system/etc/permissions/tablet_core_hardware.xml
Update that file and create android.hardware.usb.host.xml as specified by Greg-q.

adb push android.hardware.usb.host.xml /system/etc/permissions
adb push tablet_core_hardware.xml /system/etc/permissions
Reboot.



If cannot run adb push /system....

adb push test.obb /sdcard/
adb shell
su
cd /sdcard
mv test.obb /system/etc/permissions