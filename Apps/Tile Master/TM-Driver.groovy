/**
 *  ****************  Tile Master Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Tile Master data to be used with Hubitat's Dashboards.
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
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 * ------------------------------------------------------------------------------------------------------------------------------
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
 *  V2.0.3 - 02/23/20 - Make default logging false
 *  V2.0.2 - 09/22/19 - Removed 'Font Size', Also added new attribute: lastUpdated, removed several 'state.*'
 *  V2.0.1 - 09/20/19 - Initial release.
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.0 - 02/16/19 - Initially started working on this concept but never released.
 */

def setVersion(){
    appName = "TileMasterDriver"
	version = "v2.0.3" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Tile Master Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Tile%20Master/TM-Driver.groovy") {
   		capability "Actuator"

		command "sendTile01", ["string"]
		
    	attribute "tile01", "string"
        attribute "tile01Count", "number"
        attribute "lastUpdated", "string"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(""){
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
        }
    }
}
	
def sendTile01(tile1) {
    if(logEnable) log.debug "In Tile Master Driver - Received new data!"
    device1 = tile1
	device1Count = device1.length()
	if(device1Count <= 1024) {
		if(logEnable) log.debug "device1 - has ${device1Count} Characters<br>${device1}"
	} else {
		device1 = "Too many characters to display on Dashboard (${device1Count})"
	}
	sendEvent(name: "tile01", value: device1, displayed: true)
    sendEvent(name: "tile01Count", value: device1Count, displayed: true)
    
    lastUpdated = new Date()
    sendEvent( name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a") )
}
