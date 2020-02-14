/**
 *  ****************  Tile Master 2 Parent ****************
 *
 *  Design Usage:
 *  Create a tile with multiple devices and customization options.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.8 - 02/12/20 - Cosmetic changes
 *  V2.0.7 - 02/12/20 - Added default color codes
 *  V2.0.6 - 02/05/20 - Support Smoke/CO decectors (clear-detected) by @LostJen. Thanks!
 *  V2.0.5 - 11/04/19 - Fixed some typo's in the color options, thanks scubamikejax904!
 *  V2.0.4 - 09/27/19 - Fixed missing motion color options
 *  V2.0.3 - 09/22/19 - Added color options for Temperature and Battery Levels
 *  V2.0.2 - 09/21/19 - Added device value color options
 *  V2.0.1 - 09/20/19 - Initial release.
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.0 - 02/16/19 - Initially started working on this concept but never released.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "TileMaster2ParentVersion"
	state.version = "v2.0.8"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name:"Tile Master 2",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a tile with multiple devices and customization options.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Tile%20Master/TM2-parent.groovy",
)

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "There are ${childApps.size()} child apps"
    childApps.each {child ->
    	log.info "Child app: ${child.label}"
    }
    if(awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def mainPage() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display()
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Information</b>"
				paragraph "Create a tile with multiple devices and customization options."
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
                paragraph "Be sure to complete the 'Color and Level Options' section and hit 'done' BEFORE creating any child devices."
				app(name: "anyOpenApp", appName: "Tile Master 2 Child", namespace: "BPTWorld", title: "<b>Add a new 'Tile Master 2' child</b>", multiple: true)
			}
            // ** App Watchdog Code **
            section("This app supports App Watchdog 2! Click here for more Information", hideable: true, hidden: true) {
				paragraph "<b>Information</b><br>See if any compatible app needs an update, all in one place!"
                paragraph "<b>Requirements</b><br> - Must install the app 'App Watchdog'. Please visit <a href='https://community.hubitat.com/t/release-app-watchdog/9952' target='_blank'>this page</a> for more information.<br> - When you are ready to go, turn on the switch below<br> - Then select 'App Watchdog Data' from the dropdown.<br> - That's it, you will now be notified automaticaly of updates."
                input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")
			}
            if(sendToAWSwitch) {
                section(getFormat("header-green", "${getImage("Blank")}"+" App Watchdog 2")) {    
                    if(sendToAWSwitch) input(name: "awDevice", type: "capability.actuator", title: "Please select 'App Watchdog Data' from the dropdown", submitOnChange: true, required: true, multiple: false)
			        if(sendToAWSwitch && awDevice) setVersion()
                }
            }
            // ** End App Watchdog Code **
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
            section(getFormat("header-green", "${getImage("Blank")}"+" Device Value Color Config")) {}
			section("Color and Level Options:", hideable: true, hidden: false) {
                paragraph "Color is optional and is selectable within each child app. All child apps will get the values from here."
				paragraph "Enter in the colors you would like assigned to each value.<br>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White"
				input "colorOn", "text", title: "<span style='color: ${colorOn};font-size: 25px'>on</span>", submitOnChange: true, width: 6, defaultValue: "red"
                input "colorOff", "text", title: "<span style='color: ${colorOff};font-size: 25px'>off</span>", submitOnChange: true, width: 6, defaultValue: "green"
                
                input "colorOpen", "text", title: "<span style='color: ${colorOpen};font-size: 25px'>open</span>", submitOnChange: true, width: 6, defaultValue: "red"
                input "colorClosed", "text", title: "<span style='color: ${colorClosed};font-size: 25px'>closed</span>", submitOnChange: true, width: 6, defaultValue: "green"
                
                input "colorActive", "text", title: "<span style='color: ${colorActive};font-size: 25px'>active</span>", submitOnChange: true, width: 6, defaultValue: "red"
                input "colorInactive", "text", title: "<span style='color: ${colorInactive};font-size: 25px'>inactive</span>", submitOnChange: true, width: 6, defaultValue: "green"
                
                input "colorLocked", "text", title: "<span style='color: ${colorLocked};font-size: 25px'>locked</span>", submitOnChange: true, width: 6, defaultValue: "green"
                input "colorUnlocked", "text", title: "<span style='color: ${colorUnlocked};font-size: 25px'>unlocked</span>", submitOnChange: true, width: 6, defaultValue: "red"
                
                input "colorWet", "text", title: "<span style='color: ${colorWet};font-size: 25px'>wet</span>", submitOnChange: true, width: 6, defaultValue: "red"
                input "colorDry", "text", title: "<span style='color: ${colorDry};font-size: 25px'>dry</span>", submitOnChange: true, width: 6, defaultValue: "green"
                
                input "colorPresent", "text", title: "<span style='color: ${colorPresent};font-size: 25px'>present</span>", submitOnChange: true, width: 6, defaultValue: "green"
                input "colorNotPresent", "text", title: "<span style='color: ${colorNotPresent};font-size: 25px'>not present</span>", submitOnChange: true, width: 6, defaultValue: "red"

                input "colorClear", "text", title: "<span style='color: ${colorClear};font-size: 25px'>clear</span>", submitOnChange: true, width: 6, defaultValue: "green"
                input "colorDetected", "text", title: "<span style='color: ${colorDetected};font-size: 25px'>detected</span>", submitOnChange: true, width: 6, defaultValue: "red"
                
                paragraph "<b>Temperature Options</b>"
                input "tempLow", "text", title: "Temp <= LOW", submitOnChange: true, width: 6, defaultValue: "40"
                input "tempHigh", "text", title: "Temp >= HIGH", submitOnChange: true, width: 6, defaultValue: "80"
                input "colorTempLow", "text", title: "<span style='color: ${colorTempLow};font-size: 25px'>Temp <= ${tempLow}</span>", submitOnChange: true, width: 4, defaultValue: "blue"
                input "colorTemp", "text", title: "<span style='color: ${colorTemp};font-size: 25px'>Temp Between</span>", submitOnChange: true, width: 4, defaultValue: "green"
                input "colorTempHigh", "text", title: "<span style='color: ${colorTempHigh};font-size: 25px'>Temp >= ${tempHigh}</span>", submitOnChange: true, width: 4, defaultValue: "red"
                
                paragraph "<b>Battery Level Options</b>"
                input "battLow", "text", title: "Battery <= LOW", submitOnChange: true, width: 6, defaultValue: "40"
                input "battHigh", "text", title: "Battery >= HIGH", submitOnChange: true, width: 6, defaultValue: "80"
                input "colorBattLow", "text", title: "<span style='color: ${colorBattLow};font-size: 25px'>Battery <= ${battLow}</span>", submitOnChange: true, width: 4, defaultValue: "red"
                input "colorBatt", "text", title: "<span style='color: ${colorBatt};font-size: 25px'>Battery Between</span>", submitOnChange: true, width: 4, defaultValue: "blue"
                input "colorBattHigh", "text", title: "<span style='color: ${colorBattHigh};font-size: 25px'>Battery >= ${battHigh}</span>", submitOnChange: true, width: 4, defaultValue: "green"  
            }
			display2()
		}
	}
}

def installCheck(){
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  	}
  	else{
    	log.info "Parent Installed OK"
  	}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    section (getFormat("title", "${getImage("logo")}" + " Tile Master 2")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Tile Master 2 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
