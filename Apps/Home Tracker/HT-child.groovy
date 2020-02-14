/**
 *  ****************  Home Tracker 2 Child App  ****************
 *
 *  Design Usage:
 *  Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!
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
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.3.0 - 02/10/20 - More code changes to home tracking
 *  V2.2.9 - 02/10/20 - Minor tweaks to who's home tracking
 *  V2.2.8 - 01/14/20 - Tweaks to see who's home for announcements
 *  V2.2.7 - 01/11/20 - Trying to fix a problem with nameCount
 *  V2.2.6 - 01/11/20 - Delayed Welcome Home is now optional
 *  V2.2.5 - 01/11/20 - Lots of tweaks
 *  V2.2.4 - 01/10/20 - Working on locks code
 *  V2.2.3 - 01/09/20 - More changes
 *  V2.2.2 - 12/31/19 - Added flashing lights Notification options, Happy New Year!
 *  V2.2.1 - 12/28/19 - Bug fixes
 *  V2.2.0 - 12/17/19 - All New code!
 *
 */

import groovy.time.TimeCategory
import hubitat.helper.RMUtils

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "HomeTrackerChildVersion"
	state.version = "v2.3.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { //log.error "In setVersion - ${e}"
    }
}

definition(
    name: "Home Tracker 2 Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!",
    category: "",
	parent: "BPTWorld:Home Tracker 2",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Home%20Tracker/HT-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "messageOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "welcomeHomeOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "doorLockOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "ruleMachineOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
            paragraph "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message <i>after</i> entering the home!"
        	paragraph "<b>Type of 'Welcome Home' Triggers:</b>"
    		paragraph "<b>Unlock or Door Open</b><br>Both of these work pretty much the same. When door or lock is triggered, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement."
			paragraph "Each trigger can have multiple selections but this is an 'or' function. Meaning it only takes one device to trigger the actions. ie. Door1 or Door2 has been opened. If you require a different delay per door/lock, then separate child apps would be required - one for each door or lock."
			paragraph "<b>Motion Sensor</b><br>When motion sensor becomes active, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement. If you require a different delay per motion sensor, then separate child apps would be required - one for each motion sensor."
			paragraph "This trigger also works with Hubitat's built in 'Zone Motion Controllers' app. Which allows you to do some pretty cool things with motion sensors."
			paragraph "<b>Requirements:</b>"
			paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps."
            paragraph "<b>* NOTE: All Presence Sensors/Locks are now setup in the parent app, Advanced Config section.</b>"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Options, Options, Options")) {
            if(omessage || omessageD || omessageHN) {
                href "messageOptions", title:"${getImage("checkMarkGreen")} Message Options", description:"Click here to setup Home Now, Welcome Home and Departed Message options"
            } else {
                href "messageOptions", title:"Message Options", description:"Click here to setup Home Now, Welcome Home and Departed Message Options"
            }
            if(speakerMP || speakerSS) {
                href "speechOptions", title:"${getImage("checkMarkGreen")} Notification Options", description:"Click here to setup speakers, volume control , push Options and/or Flash Lights"
            } else {
                href "speechOptions", title:"Notification Options", description:"Click here to setup speakers, volume control, push Options and/or Flash Lights"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Special Features")) {
            if(triggerMode) {
                href "welcomeHomeOptions", title:"${getImage("checkMarkGreen")} Welcome Home Trigger Options", description:"Click here to setup the Welcome Home trigger options"
            } else {
                href "welcomeHomeOptions", title:"Welcome Home Options", description:"Click here to setup the Welcome Home trigger options"
            }
    	    if(parent.locks) {
                href "doorLockOptions", title:"${getImage("checkMarkGreen")} Door Lock Options", description:"Click here to setup the Door Lock options"
            } else {
                href "doorLockOptions", title:"Door Lock Options", description:"Click here to setup the Door Lock options"
            }
            
            href "ruleMachineOptions", title:"Rule Machine Options", description:"Click here to setup the Rule Machine options"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input "logEnable", "bool", defaultValue: true, title: "Enable Debug Logging", description: "Enable extra logging"
		}
		display2()
	}
}

def speechOptions(){
    dynamicPage(name: "speechOptions", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerProxy", "bool", defaultValue: false, title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange: true
        }
        if(!speakerProxy) {
            if(speakerMP || speakerSS) {
		        section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		            paragraph "NOTE: Not all speakers can use volume controls.", width:8
                    paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                    input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required:true, width:6
		            input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required:true, width:6
                    input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required:false, submitOnChange:true
		    	    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required:true, width:6
    	    	    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required:true, width:6
                }
		    }
		    section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
                input "fromTime", "time", title: "From", required:false, width: 6
        	    input "toTime", "time", title: "To", required:false, width: 6
		    }
        } else {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Proxy")) {
		        paragraph "Speaker proxy in use."
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            input "flashOnHome", "bool", defaultValue: false, title: "Flash light(s) when someone comes home", description: "Flash on Home", submitOnChange: true
            if(flashOnHome) {
                input "switchesHome", "capability.switch", title: "Flash these lights", multiple: true
		        input "numFlashesHome", "number", title: "Number of times (default: 2)", required: false, width: 6
                input "delayHome", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
            }
            input "flashOnDep", "bool", defaultValue: false, title: "Flash light(s) when someone leaves", description: "Flash on Departure", submitOnChange: true
            if(flashOnDep) {
                input "switchesDep", "capability.switch", title: "Flash these lights", multiple: true
		        input "numFlashesDep", "number", title: "Number of times (default: 2)", required: false, width: 6
                input "delayDep", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
            }
        }
    }
}

def messageOptions(){
    dynamicPage(name: "messageOptions", title: "Message Options", install: false, uninstall:false){    
        section(getFormat("header-green", "${getImage("Blank")}"+" Home NOW Message Options")) {
			input "homeNow", "bool", defaultValue: false, title: "Announce when a presence sensor arrives at 'Home', with NO wait? (off='No', on='Yes')", description: "Home Instant", submitOnChange:true
            paragraph "This will give a heads up that someone is home. But can be a false alarm if they are just driving by."
            if(homeNow) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
                paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. 'Welcome back %name%' + 'Nice to see you again'"
                paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
                
			    input "omessageHN", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				input "oMsgListHN", "bool", defaultValue: true, title: "Show a list view of the opening messages?", description: "List View", submitOnChange: true
				if(oMsgListHN) {
				    def ovaluesHN = "${omessageHN}".split(";")
				    olistMapHN = ""
    				ovaluesHN.each { item -> olistMapHN += "${item}<br>"}
					paragraph "${olistMapHN}"
				}
                input "cmessageHN", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				input "cMsgListHN", "bool", defaultValue: true, title: "Show a list view of the closing messages?", description: "List View", submitOnChange: true
				if(cMsgListHN) {
				    def cvaluesHN = "${cmessageHN}".split(";")
					clistMapHN = ""
    				cvaluesHN.each { item -> clistMapHN += "${item}<br>"}
					paragraph "${clistMapHN}"
				}
            }
        }       
		section(getFormat("header-green", "${getImage("Blank")}"+" Welcome Home Message Options")) {
            paragraph "This will speak a nice 'Welcome Home' message AFTER you have entered the house."
            input "welcomeHome", "bool", defaultValue:false, title: "Use Welcome Home features?", description: "Welcome Home", submitOnChange:true
            if(welcomeHome) {
                paragraph "<u>Optional wildcards:</u><br>%greeting% - returns a Greeting based on times selected below<br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
                paragraph "Message constructed as 'Opening message' + 'Closing message'<br>REMEMBER to use your wildcards!<br>ie. 'Welcome back %name%' + 'Nice to see you again'"
                paragraph "<hr>"
                paragraph "If a presence sensor has been present for less than x minutes, after the trigger, then speak the message."
                input "timeHome", "number", title: "How many minutes can the presence sensor be present and still be considered for a welcome home message (default=10)", required: true, defaultValue: 10  
            
                paragraph "If a presence sensor has been not present for less than x minutes, after the trigger, then speak the message."
                input "timeAway", "number", title: "How many minutes can the presence sensor be not present and still be considered for a departed message (default=2)", required: true, defaultValue: 2
                paragraph "<hr>"
                paragraph "<b>Greeting Options</b>"
                paragraph "Between what times will Greeting 1 be used"
                input "fromTimeG1", "time", title: "From", required: true, width: 6
        	    input "toTimeG1", "time", title: "To", required: true, width: 6
			    input "greeting1", "text", title: "Random Greeting - 1 (am) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
			    input "oG1List", "bool", defaultValue: false, title: "Show a list view of random messages 1?", description: "List View", submitOnChange: true
			    if(oG1List) {
				    def valuesG1 = "${greeting1}".split(";")
				    listMapG1 = ""
    			    valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
				    paragraph "${listMapG1}"
			    }
                paragraph "Between what times will Greeting 2 be used"
                input "fromTimeG2", "time", title: "From", required: true, width: 6
        	    input "toTimeG2", "time", title: "To", required: true, width: 6
			    input "greeting2", "text", title: "Random Greeting - 2 (pm before 6) - Separate each message with <b>;</b> (semicolon)",  required:true, submitOnChange:true
			    input "oG2List", "bool", defaultValue: false, title: "Show a list view of the random messages 2?", description: "List View", submitOnChange: true
			    if(oG2List) {
				    def valuesG2 = "${greeting2}".split(";")
				    listMapG2 = ""
    			    valuesG2.each { itemG2 -> listMapG2 += "${itemG2}<br>" }
				    paragraph "${listMapG2}"
			    }
                paragraph "Between what times will Greeting 3 be used"
                input "fromTimeG3", "time", title: "From", required: true, width: 6
        	    input "toTimeG3", "time", title: "To", required: true, width: 6
			    input "greeting3", "text", title: "Random Greeting - 3 (pm after 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
			    input "oG3List", "bool", defaultValue: false, title: "Show a list view of the random messages 3?", description: "List View", submitOnChange: true
			    if(oG3List) {
				    def valuesG3 = "${greeting3}".split(";")
				    listMapG3 = ""
    			    valuesG3.each { itemG3 -> listMapG3 += "${itemG3}<br>" }
				    paragraph "${listMapG3}"
			    }
			    paragraph "<hr>"
            
                paragraph "<b>Opening and Closing Options</b>"
                paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
			    input "omessage", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
			    input "oMsgList", "bool", defaultValue: true, title: "Show a list view of the opening messages?", description: "List View", submitOnChange: true
			    if(oMsgList) {
				    def ovalues = "${omessage}".split(";")
				    olistMap = ""
    			    ovalues.each { item -> olistMap += "${item}<br>"}
				    paragraph "${olistMap}"
			    }
                input "cmessage", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
			    input "cMsgList", "bool", defaultValue: true, title: "Show a list view of the closing messages?", description: "List View", submitOnChange: true
			    if(cMsgList) {
				    def cvalues = "${cmessage}".split(";")
				    clistMap = ""
    			    cvalues.each { item -> clistMap += "${item}<br>"}
				    paragraph "${clistMap}"
			    }
                paragraph "<hr>"
                input "delay1", "number", title: "How many seconds from the time the trigger being activated to the announcement being made (default=10)", required: true, defaultValue: 10
	        }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Departed Message Options")) {
            paragraph "Please only choose <b>one</b> option"
            input "departedNow", "bool", defaultValue: false, title: "Announce when the presence sensor departs, right away? (off='No', on='Yes')", description: "Home Depart", submitOnChange:true
            paragraph "<small>This will give a heads up that someone has departed. As soon as it is detected.</small>"
			input "departedDelayed", "bool", defaultValue: false, title: "Announce when the presence sensor departs, after a 2 minute delay? (off='No', on='Yes')", description: "Delayed Home Depart", submitOnChange:true
            paragraph "<small>This will give a heads up that someone has departed. But help with false announcements.</small>" 
            if(departedNow && departedDelayed) { paragraph "<b>Please only select ONE Departed option!</b>" }
            paragraph "<hr>"
            
            if(departedNow || departedDelayed) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
                paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. '%name%' + 'is no longer here'"
                paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
				input "omessageD", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				input "oMsgListD", "bool", defaultValue: true, title: "Show a list view of the opening messages?", description: "List View", submitOnChange: true
				if(oMsgListD) {
				    def ovaluesD = "${omessageD}".split(";")
				    olistMapD = ""
    				ovaluesD.each { item -> olistMapD += "${item}<br>"}
					paragraph "${olistMapD}"
				}
                input "cmessageD", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				input "cMsgListD", "bool", defaultValue: true, title: "Show a list view of the closing messages?", description: "List View", submitOnChange: true
				if(cMsgListD) {
				    def cvaluesD = "${cmessageD}".split(";")
					clistMapD = ""
    				cvaluesD.each { item -> clistMapD += "${item}<br>"}
					paragraph "${clistMapD}"
                }
            }
        }
    }
}

def welcomeHomeOptions(){
    dynamicPage(name: "welcomeHomeOptions", title: "Welcome Home Trigger Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Welcome Home Trigger Options")) { 
            paragraph "Welcome Home is a special feature that waits for you to enter the house <i>before</i> making the announcement!  Welcoming you home with a personalized message."
            input "welcomeHome", "bool", defaultValue:false, title: "Use Welcome Home features?", description: "Welcome Home", submitOnChange:true
            if(welcomeHome) {
                input "triggerMode", "enum", title: "Select activation Type", submitOnChange:true,  options: ["Contact_Sensor","Door_Lock","Motion_Sensor"], required:true, Multiple:false
			    if(triggerMode == "Door_Lock"){
				    input "lock", "capability.lock", title: "Activate the welcome message when this door is unlocked", required:true, multiple:true
			    }
			    if(triggerMode == "Contact_Sensor"){
				    input "contactSensor", "capability.contactSensor", title: "Activate the welcome message when this contact sensor is activated", required:true, multiple:true
				    input "csOpenClosed", "enum", title: "Activate when Opened or Closed" , options: ["Open","Closed"], required:true, defaultValue: "Open"
			    }
			    if(triggerMode == "Motion_Sensor"){
				    input "motionSensor", "capability.motionSensor", title: "Activate the welcome message when this motion sensor is activated", required:true, multiple:true
			    }
            }
        }
    }
}

def doorLockOptions(){
    dynamicPage(name: "doorLockOptions", title: "Door Lock Options", install: false, uninstall:false){  
        section(getFormat("header-green", "${getImage("Blank")}"+" Door Lock As Presence Sensor")) {
            paragraph "If a person doesn't have presence tracking available, they can still be included in the 'Welcome Home' message by lock code."
			paragraph "Note: Lock codes must be setup in Hubitat's Lock Code Manager."
            paragraph "<b>All Locks are now setup in the parent app, Advanced Config section.</b>"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Lock Announcements")) {
            paragraph "This will give a heads up that someone has unlocked a door."
            if(parent.locks) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Lock Code<br>%door% - returns the Name of the Lock"
				input "nMessageDU", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				input "nMsgListDU", "bool", defaultValue: true, title: "Show a list view of the messages?", description: "List View", submitOnChange: true
				if(nMsgListDU) {
					def nvaluesDU = "${nMessageDU}".split(";")
					nlistMapDU = ""
    			    nvaluesDU.each { item -> nlistMapDU += "${item}<br>"}
					paragraph "${nlistMapDU}"
				}
            } else {
                paragraph "<b>Please setup your Locks in the parent app before setting up Lock Announcements.</b>"
            }
        }     
    }
}

def ruleMachineOptions(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "ruleMachineOptions", title: "Rule Machine Options", install: false, uninstall:false){
        section(){
            paragraph "<b>Run a rule when certain conditions are met.</b>"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Rule Machine Options")) {
            paragraph "<b>If everyone leaves...</b>"
            input "rmEveryoneLeaves", "enum", title: "Select which rules to run", options: rules, multiple: true
            paragraph "<hr>"
            paragraph "<b>If nobody's home and someone returns...</b>"
            input "rmAnyoneReturns", "enum", title: "Select which rules to run", options: rules, multiple: true
            paragraph "<hr>"
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
    subscribe(parent.presenceSensors, "presence", presenceSensorHandler)
    subscribe(parent.locks, "lock", lockPresenceHandler)
    
    if(welcomeHome) {
	    if(triggerMode == "Door_Lock"){subscribe(parent.locks, "lock", lockHandler)}
	    if(triggerMode == "Contact_Sensor"){subscribe(contactSensor, "contact", contactSensorHandler)}
	    if(triggerMode == "Motion_Sensor"){subscribe(motionSensor, "motion", motionSensorHandler)}
    }
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
    
    // setup initial values
    presenceSensorHandler()
    lockPresenceHandler()
}

def presenceSensorHandler(evt){
    if(logEnable) log.warn "In presenceSensorHandler - ********************  Starting Home Tracker 2 - Presense Sensors (${state.version})  ********************"
    theGvDevice = parent.gvDevice
    
    if(parent.presenceSensors) {
        state.pSensorsSize = parent.presenceSensors.size()
        if(logEnable) log.debug "In presenceSensorHandler - presenceSensors: ${parent.presenceSensors}" 
        for(x=0;x < state.pSensorsSize.toInteger();x++) {
            if(logEnable) log.debug " --------------------  presenceSensorHandler - sensor ${x}  --------------------"
            state.pSensor = parent.presenceSensors[x].currentValue("presence")
            state.lastActivity = parent.presenceSensors[x].getLastActivity()
            getTimeDiff(x)
            
            name = "sensor" + x + "Name"
            tempName = theGvDevice.currentValue("${name}")
            tempNames = tempName.split(";")

            if(tempNames[2] != "null") {
                state.fName="${tempNames[2]}"
            } else {
                state.fName="${tempNames[1]}"
            }
            
            sensor = "sensor" + x + "BH"
            state.globalStatus = theGvDevice.currentValue("${sensor}")
            if(state.globalStatus == null || state.globalStatus == "") {
                state.globalStatus = "${x};notSet"
                state.status = state.globalStatus.split(";")
            } else {
                state.status = state.globalStatus.split(";")
            }
            
            if(logEnable) log.debug "In presenceSensorHandler - fName: ${state.fName} - globalStatus: ${state.globalStatus} - status0: ${state.status[0]} - status1: ${state.status[1]}"
         
            if(state.pSensor == "present") {
                if(logEnable) log.debug "In presenceSensorHandler (${x}) - Working On: ${parent.presenceSensors[x]} - fName: ${state.fName} - pSensor: ${state.pSensor} - status: ${state.status[1]} - TtimeDiffSecs: ${state.timeDiffSecs} - timeDiff: ${state.timeDiff}"
                if(state.timeDiffSecs < 20) {
                    if(logEnable) log.debug "In whosHomeHandler - Welcome Now - (${x}) - ${state.fName} just got here! Time Diff: ${state.timeDiff}"
                    if(flashOnHome) {
                        state.fSwitches = switchesHome
                        state.fNumFlashes = numFlashesHome
                        state.fDelay = delayHome
                    }
                    if(homeNow) {
                        if(flashOnHome) flashLights()
                        addNameToPresenceMap()
                        messageHomeNow()
                    }
                    state.globalStatus = "${x};justArrived"
                } else {
                    if(state.timeDiff < timeHome) { 
                        if(state.status[1] == "justArrived") { 
		                    if(logEnable) log.debug "In whosHomeHandler - Welcome Home - (${x}) - ${state.fName} is now home! Time Diff: ${state.timeDiff}"
                            addNameToPresenceMap()
                        } else {
                            if(logEnable) log.debug "In whosHomeHandler - Welcome Home - (${x}) - ${state.fName} - Welcome Home announcement was already made."
                        }
                    } else {
                        if(logEnable) log.debug "In whosHomeHandler - Welcome Home - (${x}) - ${state.fName} - Home too long (${state.timeDiff}). No home announcements needed."
                    }
                    state.globalStatus = "${x};beenHome"
                }
            }
     
            if(state.pSensor == "not present") {
                if(logEnable) log.debug "In whosAwayHandler (${x}) - ${state.fName} - Time Diff: ${state.timeDiff} - pSensor: ${state.pSensor}"            
                if(state.timeDiff < 20) {
                    if(logEnable) log.debug "In whosAwayHandler (${x}) - ${state.fName} just left home! Time Diff: ${state.timeDiff}"   
                    if(flashOnDep) {
                        state.fSwitches = switchesDep
                        state.fNumFlashes = numFlashesDep
                        state.fDelay = delayDep
                    }
                    if(departedNow) {
                        if(flashOnDep) flashLights()
                        addNameToPresenceMap()
                        messageDeparted()
                    }                   
                    if(departedDelayed) {
                        if(logEnable) log.debug "In whosAwayHandler - Will announce departure after a 2 minutes wait"
                        if(flashOnDep) runIn(120, flashLights)
                        addNameToPresenceMap()
                        runIn(120, messageDeparted)
                    }
                } else {
                    if(logEnable) log.debug "In whosAwayHandler (${x}) - ${state.fName} - Gone too long (${state.timeDiff}). No away announcements needed."
                }  
                state.globalStatus = "${x};notHome"
            }
            if(logEnable) log.debug "In presenceSensorHandler - Sending globalStatus: ${state.globalStatus}"
            theGvDevice.sendDataMap(state.globalStatus)
        }
    }
    if(logEnable) log.debug "In presenceSensorHandler - Finished (Presence)"
}

def lockPresenceHandler(evt){
    if(logEnable) log.warn "In lockPresenceHandler - ********************  Starting Home Tracker 2 - Lock as Presence Sensors  ********************"
    theGvDevice = parent.gvDevice
 
    if(evt) {
        lockdata = evt.data
	    lockStatus = evt.value
	    lockName = evt.displayName
	    if(logEnable) log.trace "In lockHandler (${state.version}) - Lock: ${lockName} - Status: ${lockStatus}"
	    if(lockStatus == "unlocked") {
            if(logEnable) log.trace "In lockHandler - Lock: ${lockName} - Status: ${lockStatus} - We're in!"
            if(theLocks) {
                //if(logEnable) log.trace "In lockHandler - lockdata: ${lockdata}"
                if (lockdata && !lockdata[0].startsWith("{")) {
                    lockdata = decrypt(lockdata)
                    //log.trace "Lock Data: ${lockdata}"
                    if (lockdata == null) {
                        log.debug "Unable to decrypt lock code from device: ${lockName}"
                        return
                    }
                }
                def codeMap = parseJson(lockdata ?: "{}").find{ it }
                //if(logEnable) log.trace "In lockHandler - codeMap: ${codeMap}"
                if (!codeMap) {
                    if(logEnable) log.trace "In lockHandler - Lock Code not available."
                    return
                }
                codeName = "${codeMap?.value?.name}"         
	            if(logEnable) log.trace "In lockHandler - ${lockName} was unlocked by ${fName}"	
            }
        }
    }
       
    if(codeName) {
        state.locksSize = parent.locks.size()
        if(logEnable) log.trace "In lockPresenceHandler - codeName: ${codeName}"
        for(x=0;x < state.locksSize.toInteger();x++) {
            if(logEnable) log.trace " --------------------  lockPresenceHandler - sensor ${x}  --------------------"
            state.lock = parent.locks[x].currentValue("lock")
            state.lastActivity = parent.locks[x].getLastActivity()  
            getTimeDiff(x)
            
            name = "lock" + x + "Name"
            tempName = theGvDevice.currentValue("${name}")
            tempNames = tempName.split(";")
            if(tempNames[2] != "null") {
                state.fName="${tempNames[2]}"
            } else {
                state.fName="${tempNames[1]}"
            }
        
            lock = "lock" + x + "BH"
            state.globalStatus = theGvDevice.currentValue("${lock}")
            if(state.globalStatus == null || state.globalStatus == "") {
                state.globalStatus = "${x};notSet"
                state.status = state.globalStatus.split(";")
            } else {
                state.status = state.globalStatus.split(";")
            }
            
            if(logEnable) log.trace "In lockPresenceHandler (${x}) - Locks 1 - Working On: ${parent.locks[x]} - fName: ${state.fName} - lock: ${state.lock}"
        
            if(state.lock == "unlocked") {            
                if(logEnable) log.trace "In lockPresenceHandler (${x}) - Locks 2 - Working On: ${parent.locks[x]} - fName: ${state.fName} - lock: ${state.lock} - status: ${state.status[1]}"
                if(state.timeDiffSecs < 20) {
                    if(logEnable) log.debug "In whosHomeHandler - Home Now - (${x}) - ${state.fName} just unlocked the door! Time Diff: ${state.timeDiff}"
                    if(flashOnHome) {
                        state.fSwitches = switchesHome
                        state.fNumFlashes = numFlashesHome
                        state.fDelay = delayHome
                    }
                    if(homeNow) {
                        if(flashOnHome) flashLights()
                        addNameToPresenceMap()
                        messageHomeNow()
                    }
                    state.globalStatus = "${x};justArrived"
                } else {
                    if(state.timeDiff < timeHome) { 
                        if(state.status[1] == "justArrived") { 
		                    if(logEnable) log.debug "In whosHomeHandler - Welcome Home - (${x}) - ${state.fName} is now home! Time Diff: ${state.timeDiff}"
                            addNameToPresenceMap()
                        } else {
                            if(logEnable) log.debug "In whosHomeHandler - Welcome Home - (${x}) - ${state.fName} - Welcome Home announcement was already made."
                        }
                    } else {
                       if(logEnable) log.debug "In whosHomeHandler - Welcome Home - (${x}) - ${state.fName} - Home too long (${state.timeDiff}). No home announcements needed."
                    }
                    state.globalStatus = "${x};beenHome"
                }
            } else if(state.lock == "locked") {
                state.globalStatus = "${x};notHome"
                if(logEnable) log.trace "In lockPresenceHandler (${x}) - Lock - Working On: ${parent.locks[x]} - fName: ${state.fName} - lock: ${state.lock}"
            }
            if(logEnable) log.debug "In lockPresenceHandler - Sending globalStatus: ${state.globalStatus}"
            theGvDevice.sendDataMapLock(state.globalStatus)
        }
    }
    if(logEnable) log.debug "In lockPresenceHandler - Finished (Lock)"
}

def addNameToPresenceMap() {
    if(logEnable) log.debug "In addNameToPresenceMap (${state.version})"
	state.nameCount = state.nameCount + 1
    if(state.nameCount == 1) state.presenceMap = ["${state.fName}"]
	if(state.nameCount >= 2) state.presenceMap += ["${state.fName}"]
    if(logEnable) log.info "In addNameToPresenceMap - AFTER - nameCount: ${state.nameCount} - presenceMap: ${state.presenceMap}"
}

def lockHandler(evt) {
	lockStatus = evt.value
	lockName = evt.displayName
	if(logEnable) log.trace "In lockHandler (${state.version}) - Lock: ${lockName} - Status: ${lockStatus}"
	if(lockStatus == "unlocked") {
        if(logEnable) log.trace "In lockHandler - Lock: ${lockName} - Status: ${lockStatus} - We're in!"
        presenceSensorHandler()
	    if(state.presenceMap) messageWelcomeHome()
	}
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	state.contactName = evt.displayName
	if(logEnable) log.debug "In contactSensorHandler (${state.version}) - Contact: ${state.contactName} - Status: ${state.contactStatus}"
	if(csOpenClosed == "Open") {
		if(state.contactStatus == "open") {
			if(logEnable) log.debug "In contactSensorHandler - open"
            presenceSensorHandler()
			if(state.presenceMap) messageWelcomeHome()
		}
	}
	if(csOpenClosed == "Closed") {
		if(state.contactStatus == "closed") {
			if(logEnable) log.debug "In contactSensorHandler - closed"
            presenceSensorHandler()
			if(state.presenceMap) messageWelcomeHome()
		}
	}
}

def motionSensorHandler(evt) {
	state.motionStatus = evt.value
	state.motionName = evt.displayName
	if(logEnable) log.debug "In motionSensorHandler (${state.version}) - Motion Name: ${state.motionName} - Status: ${state.motionStatus}"
	if(state.motionStatus == "active") {
		if(logEnable) log.debug "In motionSensorHandler - active"
        presenceSensorHandler()
        if(state.presenceMap) messageWelcomeHome()
	}
}

def getTimeDiff(x) {
    if(logEnable) log.debug "In getTimeDiff (${state.version}) (${x}) - lastActivity: ${state.lastActivity}"
    
    long timeDiff
   	def now = new Date()
    def prev = Date.parse("yyy-MM-dd HH:mm:ss","${state.lastActivity}".replace("+00:00","+0000"))

    long unxNow = now.getTime()
    long unxPrev = prev.getTime()
    unxNow = unxNow/1000
    unxPrev = unxPrev/1000
    state.timeDiffSecs = Math.abs(unxNow-unxPrev)         // Seconds
    state.timeDiff = Math.round(state.timeDiffSecs/60)    // Minutes
}

def messageHomeNow() {
    checkTimeForGreeting()
	if(logEnable) log.debug "In messageHomeNow (${state.version})"
    state.message = ""

	def ovaluesHN = "${omessageHN}".split(";")
	ovSizeHN = ovaluesHN.size()
	ocountHN = ovSizeHN.toInteger()
    def orandomKeyHN = new Random().nextInt(ocountHN)

    def cvaluesHN = "${cmessageHN}".split(";")
	cvSizeHN = cvaluesHN.size()
	ccountHN = cvSizeHN.toInteger()
    def crandomKeyHN = new Random().nextInt(ccountHN)

	state.message = ovaluesHN[orandomKeyHN] + ". " + cvaluesHN[crandomKeyHN]
	if(logEnable) log.debug "In messageHomeNow - Random - ovSizeHN: ${ovSizeHN}, orandomKeyHN: ${orandomKeyHN}; Random - cvSizeHN: ${cvSizeHN}, crandomKeyHN: ${crandomKeyHN}, message: ${state.message}"
	messageHandler()
	letsTalk()
}

def messageWelcomeHome() {   // Uses a modified version of @Matthew opening and closing message code
    checkTimeForGreeting()
	if(logEnable) log.debug "In messageWelcomeHome (${state.version})"
    state.message = ""

	def ovalues = "${omessage}".split(";")
	ovSize = ovalues.size()
	ocount = ovSize.toInteger()
    def orandomKey = new Random().nextInt(ocount)

    def cvalues = "${cmessage}".split(";")
	cvSize = cvalues.size()
	ccount = cvSize.toInteger()
    def crandomKey = new Random().nextInt(ccount)

    state.message = ovalues[orandomKey] + ". " + cvalues[crandomKey]
	if(logEnable) log.debug "In messageWelcomeHome - Random - ovSize: ${ovSize}, orandomKey: ${orandomKey}; Random - cvSize: ${cvSize}, crandomKey: ${crandomKey}, message: ${state.message}"
	messageHandler()
    if(delay1 == null || delay1 == "") delay1 = 5
    if(logEnable) log.debug "In messageWelcomeHome - Waiting ${delay1} seconds to Speak"
	def delay1ms = delay1 * 1000
	pauseExecution(delay1ms)
    if(logEnable) log.debug "In messageWelcomeHome - going to letsTalk with the message"
	letsTalk()
}

def messageDeparted() {
    checkTimeForGreeting()
    if(logEnable) log.debug "In messageDeparted (${state.version})"
    state.message = ""
    
	def ovaluesD = "${omessageD}".split(";")
	ovSizeD = ovaluesD.size()
	ocountD = ovSizeD.toInteger()
    def orandomKeyD = new Random().nextInt(ocountD)

    def cvaluesD = "${cmessageD}".split(";")
	cvSizeD = cvaluesD.size()
	ccountD = cvSizeD.toInteger()
    def crandomKeyD = new Random().nextInt(ccountD)

	state.message = ovaluesD[orandomKeyD] + ". " + cvaluesD[crandomKeyD]
	if(logEnable) log.debug "In messageDeparted - Random - ovSizeD: ${ovSizeD}, orandomKeyD: ${orandomKeyD}; Random - cvSizeD: ${cvSizeD}, crandomKeyD: ${crandomKeyD}, message: ${state.message}"
    messageHandler()
	letsTalk()
}

def messageHandler() {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    if(state.message.contains("%greeting%")) {state.message = state.message.replace('%greeting%', "${state.greeting}" )}
	if(state.message.contains("%name%")) {state.message = state.message.replace('%name%', getName() )}
	if(state.message.contains("%is_are%")) {state.message = state.message.replace('%is_are%', "${is_are}" )}
	if(state.message.contains("%has_have%")) {state.message = state.message.replace('%has_have%', "${has_have}" )}
    if(logEnable) log.debug "In messageHandler - message: ${state.message}"
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk (${state.version})"
	checkTime()
	checkVol()
    if(state.timeBetween == true) {
		theMsg = state.message
        speechDuration = Math.max(Math.round(theMsg.length()/12),2)+3		// Code from @djgutheinz
        speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, theMsg: ${theMsg}, volRestore: ${volRestore}"
        state.speakers.each { it ->
            if(logEnable) log.debug "Speaker in use: ${it}"
            if(speakerProxy) {
                if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                it.speak(theMsg)
            } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                def prevVolume = it.currentValue("volume")
                it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
            } else if(it.hasCommand('playTextAndRestore')) {   
                if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                def prevVolume = it.currentValue("volume")
                it.playTextAndRestore(theMsg, prevVolume)
            } else {		        
                if(logEnable) log.debug "In letsTalk - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                it.speak(theMsg)
                pauseExecution(speechDuration2)
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
            }
        }
	    if(logEnable) log.debug "In letsTalk - Finished speaking"  
	    log.info "${app.label} - ${theMsg}"
        if(sendPushMessage) pushNow(theMsg)
	} else {
        if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
    theMsg = ""
    clearPresenceMap()
}

def checkVol(){
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - setting volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def checkTimeForGreeting() {
    if(logEnable) log.debug "In checkTimeForGreeting (${state.version}) - G1 - ${fromTimeG1} - ${toTimeG1}"
    state.betweenTimeG1 = timeOfDayIsBetween(toDateTime(fromTimeG1), toDateTime(toTimeG1), new Date(), location.timeZone)
    if(state.betweenTimeG1) {
		def values = "${greeting1}".split(";")
	    vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		state.greeting = values[randomKey]
		if(logEnable) log.debug "In checkTimeForGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm: ${timeampm} - timehh: ${timeHH}"
    }
    
    if(logEnable) log.debug "In checkTimeForGreeting (${state.version}) - G2 - ${fromTimeG2} - ${toTimeG2}"
    state.betweenTimeG2 = timeOfDayIsBetween(toDateTime(fromTimeG2), toDateTime(toTimeG2), new Date(), location.timeZone)
    if(state.betweenTimeG2) {
		def values = "${greeting2}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		state.greeting = values[randomKey]
		if(logEnable) log.debug "In checkTimeForGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm: ${timeampm} - timehh: ${timeHH}"
    }

    if(logEnable) log.debug "In checkTimeForGreeting (${state.version}) - G3 - ${fromTimeG3} - ${toTimeG3}"
    state.betweenTimeG3 = timeOfDayIsBetween(toDateTime(fromTimeG3), toDateTime(toTimeG3), new Date(), location.timeZone)
    if(state.betweenTimeG3) {
		def values = "${greeting3}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		state.greeting = values[randomKey]
		if(logEnable) log.debug "In checkTimeForGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm = ${timeampm} - timehh = ${timeHH}"
    }
}

private getName() {
    if(logEnable) log.debug "In getName (${state.version})"
    if(state.presenceMap) {
        presenceMap = state.presenceMap.unique()
        nameCount = presenceMap.size()
        nameCountM1 = nameCount - 1
        myCount = 0
	    name = ""
    
        if(logEnable) log.debug "In getName - Number of Names: ${nameCount} - Names: ${presenceMap}"
        
        presenceMap.each { it -> 
		    myCount = myCount + 1
            if(!name.contains("${it}")) {
                if(myCount == 1) name = "${it}"
                if(myCount > 1) {
                    if(myCount <= nameCountM1) name = "${name}" + ", ${it}"
			        if(myCount == nameCount) name = "${name}" + " and ${it}"
                }
            }
            if(logEnable) log.debug "In getName - Working On - nameCount: ${nameCount} - myCount: ${myCount} - name: ${name}"
        }
   
        is_are = (name.contains(' and ') ? 'are' : 'is')
	    has_have = (name.contains(' and ') ? 'have' : 'has')
    
	    if(name == null || name == "") names = "Whoever you are"
    
	    if(logEnable) log.debug "Name: ${name}"
	    return name 
    }
}

def clearPresenceMap() {
    if(logEnable) log.debug "In clearPresenceMap (${state.version})"
    state.presenceMap = [:]
	state.nameCount = 0
    state.clearMap = false
    state.message = ""
    if(logEnable) log.info "In clearPresenceMap - nameCount: ${state.nameCount} - presenceMap: ${state.presenceMap}"
}

def rulesHandler(rules) {
    if(logEnable) log.debug "In rulesHandler (${state.version}) - rules: ${rules}"
    RMUtils.sendAction(rules, "runRule", app.label)
}

def pushNow(msg) {
	if(logEnable) log.debug "In pushNow (${state.version})"
	if(sendPushMessage) {
		pushMessage = "${app.label} \n"
		pushMessage += msg
		if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
        sendPushMessage.deviceNotification(pushMessage)
	}	
}

private flashLights() {    // Modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
	def doFlash = true
	def delay = state.fDelay ?: 500
	def numFlashes = state.fNumFlashes ?: 2

	if(logEnable) log.debug "In flashLights - LAST ACTIVATED: ${state.lastActivated}"
	if(state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (delay)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash - ELAPSED: $elapsed - LAST ACTIVATED: ${state.lastActivated}"
	}

	if(doFlash) {        // Modified from ST documents
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		state.lastActivated = now()
		if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = state.fSwitches.collect{it.currentSwitch != "on"}

		numFlashes.times {
			if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
			state.fSwitches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.on()
				}
				else {
                    pauseExecution(delay)
					s.off()
				}
			}
			if(logEnable) log.debug "In flashLights - Switch off after $delay milliseconds"
			state.fSwitches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.off()
				}
				else {
                    pauseExecution(delay)
					s.on()
				}
			}
		}
	}
}

// ********** Normal Stuff **********

def setDefaults(){
	clearPresenceMap()
	if(settings.logEnable == null){settings.logEnable = false}
    if(settings.speakerProxy == null){settings.speakerProxy = false}
    if(settings.homeNow == null){settings.homeNow = false}
    if(settings.departedNow == null){settings.departedNow = false}
    if(settings.departedDelayed == null){settings.departedDelayed = false}
    if(state.clearMap == null) state.clearMap = false
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
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " Home Tracker 2 - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Home Tracker 2 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
