w732_accfix
===========

Accessory detection kernel driver fix for Philips w732 (MTK 6575)

This fix provides correct short and long press of the single headset bungle supplied with Philips w732.
During idle call state it generates short and long press with scan code 226 (HEADSETHOOK) that is mapped into keyCode 79.
During call ringing and active states it generates short and long press as call_accept and call_decline. (KEY_SEND and KEY_HANGEUL)

Since there are no full kernel sources for MTK 6775 available to me I had to write kernel loadable module and an app.

Installation:
Unpack accfix.ko_apk.zip into root of your android /sdcard
Install accfix.apk via file explorer.
Run accapp.
Press 'Install Driver' button.