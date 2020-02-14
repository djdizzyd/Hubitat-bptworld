/**
 *  ****************  Tile Master 2 Child App  ****************
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.1.6 - 02/11/20 - BIG changes - Streamlined code, reduced by over 1000 lines! (Wow!)
 *            - Each child app will now automatically create the Tile Device if needed
 *            - Each Tile can now have 9 lines, built for anyones needs! (remember, you still can only have 1024 characters)
 *            - Added Text Decoration, Bold and Italic to each line options
 *            - Each section can change color based on Device Value
 *  ---
 *  V1.0.0 - 02/16/19 - Initially started working on this concept but never released.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "TileMaster2ChildVersion"
	state.version = "v2.1.6"
   
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Tile Master 2 Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a tile with multiple devices and customization options.",
    category: "",
	parent: "BPTWorld:Tile Master 2",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Tile%20Master/TM2-child.groovy",
)

preferences {
    page name: "pageConfig"
	page name: "lineOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Create a tile with multiple devices and customization options."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {
            paragraph "Each child app needs a virtual device to store the Tile Master data. Enter a short descriptive name for this device."
			input "userName", "text", title: "Enter a name for this Tile Device (ie. 'House Temps' will become 'TM - House Temps')", required:true, submitOnChange:true
            paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
            if(userName) createChildDevice()
            paragraph "${statusMessage}"
            
            input(name: "tileDevice", type: "capability.actuator", title: "Vitual Device created to send the data to:", required: true, multiple: false)
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Line Options")) {
            input "howManyLines", "number", title: "How many lines on Tile (range: 1-9)", range: '1..9', submitOnChange:true
            input "lineToEdit", "number", title: "Which line to edit", submitOnChange:true
            if(lineToEdit > howManyLines) {paragraph "<b>Please enter a valid line number.</b>"}
        }
        if((lineToEdit > 0) && (lineToEdit <= howManyLines)) {
            x = lineToEdit
            state.lastActiv = "no"
		    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Table Options")) {           
                input "nSections_$x", "enum", title: "Number of Sections", required: false, multiple: false, options: ["1","2","3"], submitOnChange: true
                nSection = app."nSections_$x"            
			    if(nSection == "1") {
                    input "secWidth_$x", "number", title: "Section 1 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:6
			    } else if(nSection == "2") {
				    input "secWidth_$x", "number", title: "Section 1 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:6
				    input "secWidtha_$x", "number", title: "Section 2 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:6
			    } else if(nSection == "3") {
			    	input "secWidth_$x", "number", title: "Section 1 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:4
				    input "secWidtha_$x", "number", title: "Section 2 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:4
				    input "secWidthb_$x", "number", title: "Section 3 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:4
			    }

			    if(nSection == "1") {
                    secWidth = app."secWidth_$x"
				    if(secWidth == null) {secWidth = 100}
				    tableLength = secWidth
			    } else if(nSection == "2") {
                    secWidth = app."secWidth_$x"
                    secWidtha = app."secWidtha_$x"
				    if(secWidth == null) {secWidth = 50}
				    if(secWidtha == null) {secWidtha = 50}
				    tableLength = secWidth + secWidtha
			    } else if(nSection == "3") {
                    secWidth = app."secWidth_$x"
                    secWidtha = app."secWidtha_$x"
                    secWidthb = app."secWidthb_$x"
				    if(secWidth == null) {secWidth = 35}
				    if(secWidtha == null) {secWidtha = 30}
				    if(secWidthb == null) {secWidthb = 35}
				    tableLength = secWidth + secWidtha + secWidthb
			    }
            
			    if(tableLength == 100) {
				    paragraph "Table Width: <font color='green'>${tableLength}</font><br><small>* Total table width must equal 100</small>"
			    } else {
				    paragraph "Table Width: <font color='red'>${tableLength}<br><small>* Total table width must equal 100</small></font>"
			    }
		    }
            
		    if(nSection == "1" || nSection == "2" || nSection == "3") {
			    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Section 1 Options")) {
				    paragraph "<b>SECTION 1</b>"
                    paragraph "Wildcards: %lastAct% = use in any text field. Will be replaced with the selected devices Last Activity date/time"
                    paragraph "To enter in a web link, simply replace the http with wlink. ie. wlink://bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
				    input "wordsBEF_$x", "text", title: "Text BEFORE Device Status", required: false, submitOnChange: true, width:6
				    input "wordsAFT_$x", "text", title: "Text AFTER Device Status", required: false, submitOnChange: true, width:6
                
                    wordsBEF = app."wordsBEF_$x"
                    wordsAFT = app."wordsAFT_$x"
                
                    if(wordsBEF) if(wordsBEF.toLowerCase().contains("wlink")) {
                        input "linkBEF_$x", "text", title: "Text Before is a link. Please enter a friendly name to display on tile.", submitOnChange: true
                    }               
                    if(wordsAFT) if(wordsAFT.toLowerCase().contains("wlink")) {
                        input "linkAFT_$x", "text", title: "Text After is a link. Please enter a friendly name to display on tile.", submitOnChange: true
                    }

                    if(wordsBEF) {if(wordsBEF.contains("lastAct")) state.lastActiv = "yes"}
                    if(wordsAFT) {if(wordsAFT.contains("lastAct")) state.lastActiv = "yes"}  
                
				    input "device_$x", "capability.*", title: "Device", required:false, multiple:false, submitOnChange:true

                    theDevice = app."device_$x"
                    
				    if(theDevice) {
					    def allAtts = [:]
					    allAtts = theDevice.supportedAttributes.unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
					    input "deviceAtts_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAtts
                        input "hideAttr_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                        deviceAtts = app."deviceAtts_$x"
					    deviceStatus = theDevice.currentValue("${deviceAtts}")
					    if(deviceStatus == null || deviceStatus == "") deviceStatus = "No Data"
					    if(theDevice && deviceAtts) paragraph "Current Status of Device Attribute: ${theDevice} - ${deviceAtts} - ${deviceStatus}"
				    }
                    paragraph "<hr>"
                    paragraph "Style Attributes - Using default values will save on character counts."
				    input "fontSize_$x", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:6
				    input "align_$x", "enum", title: "Alignment (Center = Default)", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Center", submitOnChange: true, width: 6
                    input "useColors_$x", "bool", title: "Use custom colors on device value", defaultValue: false, description: "Colors", submitOnChange: true
                    uC = app."useColors_$x"
                    if(uC) {
                        input "valueOrCell_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                    }
                    input "color_$x", "text", title: "Text Color (Black = Default) ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Black", submitOnChange: true
                    
                    input "italic_$x", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:6
                    input "bold_$x", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:6
                    input "decoration_$x", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                }
		    }
            
		    if(nSection == "2" || nSection == "3") {
			    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Section 2 Options")) {
				    paragraph "<b>SECTION 2</b>"
                    paragraph "Wildcards: %lastAct% = use in any text field. Will be replaced with the selected devices Last Activity date/time"
                    paragraph "To enter in a web link, simply replace the http with wlink. ie. wlink://bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
				    input "wordsBEFa_$x", "text", title: "Text BEFORE Device Status", required: false, submitOnChange: true, width:6
				    input "wordsAFTa_$x", "text", title: "Text AFTER Device Status", required: false, submitOnChange: true, width:6
                
                    wordsBEFa = app."wordsBEFa_$x"
                    wordsAFTa = app."wordsAFTa_$x"
                
                    if(wordsBEFa) if(wordsBEFa.toLowerCase().contains("wlink")) {
                        input "linkBEFa_$x", "text", title: "Text Before is a link. Please enter a friendly name to display on tile.", submitOnChange: true
                    }               
                    if(wordsAFTa) if(wordsAFTa.toLowerCase().contains("wlink")) {
                        input "linkAFTa_$x", "text", title: "Text After is a link. Please enter a friendly name to display on tile.", submitOnChange: true
                    }

                    if(wordsBEFa) {if(wordsBEFa.contains("lastAct")) state.lastActiv = "yes"}
                    if(wordsAFTa) {if(wordsAFTa.contains("lastAct")) state.lastActiv = "yes"}  
                
				    input "devicea_$x", "capability.*", title: "Device", required:false, multiple:false, submitOnChange:true
                
                    theDevicea = app."devicea_$x"
                
				    if(theDevicea) {
					    def allAttsa = [:]
					    allAttsa = theDevicea.supportedAttributes.unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
					    input "deviceAttsa_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAttsa
                        input "hideAttra_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true               
					    deviceAttsa = app."deviceAttsa_$x"
					    deviceStatusa = theDevicea.currentValue("${deviceAttsa}")
					    if(deviceStatusa == null || deviceStatusa == "") deviceStatusa = "No Data"
					    if(theDevicea && deviceAttsa) paragraph "Current Status of Device Attribute: ${theDevicea} - ${deviceAttsa} - ${deviceStatusa}"
				    }
				    paragraph "<hr>"
                    paragraph "Style Attributes - Using default values will save on character counts."
				    input "fontSizea_$x", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:6
				    input "aligna_$x", "enum", title: "Alignment (Center = Default)", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Center", submitOnChange: true, width: 6
				    input "useColorsa_$x", "bool", title: "Use custom colors on device value", defaultValue: false, description: "Colors", submitOnChange: true
                    uCa = app."useColorsa_$x"
                    if(uCa) {
                        input "valueOrCella_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                    }
                    input "colora_$x", "text", title: "Text Color (Black = Default) ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Black", submitOnChange: true

                    input "italica_$x", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:6
                    input "bolda_$x", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:6
                    input "decorationa_$x", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                }
		    }
            
		    if(nSection == "3") {
			    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Section 3 Options")) {
				    paragraph "<b>SECTION 3</b>"
                    paragraph "Wildcards: %lastAct% = use in any text field. Will be replaced with the selected devices Last Activity date/time"
                    paragraph "To enter in a web link, simply replace the http with wlink. ie. wlink://bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
				    input "wordsBEFb_$x", "text", title: "Text BEFORE Device Status", required: false, submitOnChange: true, width:6
				    input "wordsAFTb_$x", "text", title: "Text AFTER Device Status", required: false, submitOnChange: true, width:6
                
                    wordsBEFb = app."wordsBEFb_$x"
                    wordsAFTb = app."wordsAFTb_$x"
                
                    if(wordsBEFb) if(wordsBEFb.toLowerCase().contains("wlink")) {
                        input "linkBEFb_$x", "text", title: "Text Before is a link. Please enter a friendly name to display on tile.", submitOnChange: true
                    }               
                    if(wordsAFTb) if(wordsAFTb.toLowerCase().contains("wlink")) {
                        input "linkAFTb_$x", "text", title: "Text After is a link. Please enter a friendly name to display on tile.", submitOnChange: true
                    }

                    if(wordsBEFb) {if(wordsBEFb.contains("lastAct")) state.lastActiv = "yes"}
                    if(wordsAFTb) {if(wordsAFTb.contains("lastAct")) state.lastActiv = "yes"}  
                
				    input "deviceb_$x", "capability.*", title: "Device", required:false, multiple:false, submitOnChange:true
                
                    theDeviceb = app."deviceb_$x"
                
				    if(theDeviceb) {
					    def allAttsb = [:]
					    allAttsb = theDeviceb.supportedAttributes.unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
					    input "deviceAttsb_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAttsb
                        input "hideAttrb_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true               
                        deviceAttsb = app."deviceAttsb_$x"
					    deviceStatusb = theDeviceb.currentValue("${deviceAttsb}")
					    if(deviceStatusb == null || deviceStatusb == "") deviceStatusb = "No Data"
					    if(theDeviceb && deviceAttsb) paragraph "Current Status of Device Attribute: ${theDeviceb} - ${deviceAttsb} - ${deviceStatusb}"
				    }
				    paragraph "<hr>"
                    paragraph "Style Attributes - Using default values will save on character counts."
				    input "fontSizeb_$x", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:6
				    input "alignb_$x", "enum", title: "Alignment (Center = Default)", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Center", submitOnChange: true, width: 6
				    input "useColorsb_$x", "bool", title: "Use custom colors on device value", defaultValue: false, description: "Colors", submitOnChange: true
                    uCb = app."useColorsb_$x"
                    if(uCb) {
                        input "valueOrCellb_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                    }
                    input "colorb_$x", "text", title: "Text Color (Black = Default) ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Black", submitOnChange: true
                 
                    input "italicb_$x", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:6
                    input "boldb_$x", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:6
                    input "decorationb_$x", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                }
		    }
        }
        if(state.lastActiv == "yes") {
            section(getFormat("header-green", "${getImage("Blank")}"+" Last Activity Formatting")) {
                input "dateTimeFormat", "enum", title: "Select Formatting", required: true, multiple: false, submitOnChange: true, options: [
                    ["f1":"MMM dd, yyy - h:mm:ss a"],
                    ["f2":"dd MMM, yyy - h:mm:ss a"],
                    ["f3":"MMM dd - h:mm:ss a (12 hour)"],
                    ["f4":"dd MMM - h:mm:ss a (12 hour)"],
                    ["f5":"MMM dd - HH:mm (24 hour)"],
                    ["f6":"dd MMM - HH:mm (24 hour)"],
                    ["f7":"h:mm:ss a (12 hour)"],
                    ["f8":"HH:mm:ss (24 hour)"],
                ]
            }
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        
		sampleTileHandler()
        
        section() {
            input "logEnable", "bool", defaultValue: "false", title: "Enable Debug Logging", description: "debugging", submitOnChange: true
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings} (${state.version})"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()

    for(x=1;x <= howManyLines;x++) {
        theDev = app."device_$x"
        theAtt = app."deviceAtts_$x"
        if(theDev) subscribe(theDev, theAtt, sampleTileHandler)
        
        theDeva = app."devicea_$x"
        theAtta = app."deviceAttsa_$x"
        if(theDeva) subscribe(theDeva, theAtta, sampleTileHandler)
        
        theDevb = app."deviceb_$x"
        theAttb = app."deviceAttsb_$x"
        if(theDevb) subscribe(theDevb, theAttb, sampleTileHandler)
    }
      
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def tileHandler(){
    if(logEnable) log.debug "*************************  Here we go  *************************"
	if(logEnable) log.debug "In tileHandler (${state.version})"
    
    for(x=1;x <= howManyLines;x++) {
        if(logEnable) log.debug "<b>**********  Starting Line $x  **********</b>"
        nSections = app."nSections_$x"
        theDevice = app."device_$x"
        theDevicea = app."devicea_$x"
        theDeviceb = app."deviceb_$x"
        deviceAtts = app."deviceAtts_$x"
        deviceAttsa = app."deviceAttsa_$x"
        deviceAttsb = app."deviceAttsb_$x"
        useColors = app."useColors_$x"
        useColorsa = app."useColorsa_$x"
        useColorsb = app."useColorsb_$x"
        valueOrCell = app."valueOrCell_$x"
        valueOrCella = app."valueOrCella_$x"
        valueOrCella = app."valueOrCellb_$x"
        
        if(logEnable) log.debug "<b>In tileHandler - Line: ${x} - nSections: ${nSections} - theDevice: ${theDevice} - deviceStatus: ${deviceStatus} - deviceAtts: ${deviceAtts} - useColors: ${useColors}</b>"
        
	    if(nSections >= "1") {
		    if(theDevice) {
			    deviceStatus = theDevice.currentValue("${deviceAtts}")
                if(deviceStatus == null || deviceStatus == "") deviceStatus = "No Data"
                if(!valueOrCell) {
                    getStatusColors(deviceStatus, deviceAtts)
                    pauseExecution(500)
                    if(logEnable) log.debug "In tileHandler - Received: ${deviceStatus1}"
                    deviceStatus = deviceStatus1
                } else {
                    getCellColors(deviceStatus, deviceAtts)
                    cellColor = theCellColor
                }
            }
	    }

	    if(nSections >= "2") {
		    if(theDevicea) {
			    deviceStatusa = theDevicea.currentValue("${deviceAttsa}")
			    if(deviceStatusa == null || deviceStatusa == "") deviceStatusa = "No Data"
                if(!valueOrCella) {
                    getStatusColors(deviceStatusa, deviceAttsa)
                    pauseExecution(500)
                    if(logEnable) log.debug "In tileHandlera - Received: ${deviceStatus1}"
                    deviceStatusa = deviceStatus1
                } else {
                    getCellColors(deviceStatusa, deviceAttsa)
                    cellColora = theCellColor
                }
		    }
	    }
        
	    if(nSections == "3") {
		    if(theDeviceb) {
			    deviceStatusb = theDeviceb.currentValue("${deviceAttsb}")
			    if(deviceStatusb == null || deviceStatusb == "") deviceStatusb = "No Data"
                if(!valueOrCellb) {
                    getStatusColors(deviceStatusb, deviceAttsb)
                    pauseExecution(500)
                    if(logEnable) log.debug "In tileHandlerb - Received: ${deviceStatus1}"
                    deviceStatusb = deviceStatus1
                } else {
                    getCellColors(deviceStatusb, deviceAttsb)
                    cellColorb = theCellColor
                }
		    }
	    }
	
// ***** Make the table for line x	*****

        theTileMap = ""

        align = app."align_$x"
        color = app."color_$x"
        fontSize = app."fontSize_$x"
        italic = app."italic_$x"
        bold = app."bold_$x"
        decoration = app."decoration_$x"
        secWidth = app."secWidth_$x"
        hideAttr = app."hideAttr_$x"
        wordsBEF = app."wordsBEF_$x"
        wordsAFT = app."wordsAFT_$x"
        linkBEF = app."linkBEF_$x"
        linkAFT = app."linkAFT_$x"
        
        aligna = app."aligna_$x"
        colora = app."colora_$x"
        fontSizea = app."fontSizea_$x"
        italica = app."italica_$x"
        bolda = app."bolda_$x"
        decorationa = app."decorationa_$x"
        secWidtha = app."secWidtha_$x"
        hideAttra = app."hideAttra_$x"
        wordsBEFa = app."wordsBEFa_$x"
        wordsAFTa = app."wordsAFTa_$x"
        linkBEFa = app."linkBEFa_$x"
        linkAFTa = app."linkAFTa_$x"
        
        alignb = app."alignb_$x"
        colorb = app."colorb_$x"
        fontSizeb = app."fontSizeb_$x"
        italicb = app."italicb_$x"
        boldb = app."boldb_$x"
        decorationb = app."decorationb_$x"
        secWidthb = app."secWidthb_$x"
        hideAttrb = app."hideAttrb_$x"
        wordsBEFb = app."wordsBEFb_$x"
        wordsAFTb = app."wordsAFTb_$x"
        linkBEFb = app."linkBEFb_$x"
        linkAFTb = app."linkAFTb_$x"
        
        theStyle = "style='width:${secWidth}%;"
        if(align != "Center") theStyle += "text-align:${align};"
        if(color != "Black") theStyle += "color:${color};"
        if(fontSize != 0) theStyle += "font-size:${fontSize}px;"
        if(italic) theStyle += "font-style:italic;"
        if(bold) theStyle += "font-weight:bold;"
        if(decoration != "None") theStyle += "text-decoration:${decoration};"
        if(valueOrCell) theStyle += "background:${cellColor};"
        
        theStyle += "'"
        
        theStylea = "style='width:${secWidtha}%;"
        if(aligna != "Center") theStylea += "text-align:${aligna};"
        if(colora != "Black") theStylea += "color:${colora};"
        if(fontSizea != 0) theStylea += "font-size:${fontSizea}px;"
        if(italica) theStylea += "font-style:italic;"
        if(bolda) theStylea += "font-weight:bold;"
        if(decorationa != "None") theStylea += "text-decoration:${decorationa};"
        if(valueOrCella) theStyle += "background:${cellColora};"
        
        theStylea += "'"
        
        theStyleb = "style='width:${secWidthb}%;"
        if(alignb != "Center") theStyleb += "text-align:${alignb};"
        if(colorb != "Black") theStyleb += "color:${colorb};"
        if(fontSizeb != 0) theStyleb += "font-size:${fontSizeb}px;"
        if(italicb) theStyleb += "font-style:italic;"
        if(boldb) theStyleb += "font-weight:bold;"
        if(decorationb != "None") theStyleb += "text-decoration:${decorationb};"
        if(valueOrCellb) theStyle += "background:${cellColorb};"
        
        theStyleb += "'"
        
        theTileMap = "<table style='width:100%'><tr>"
        
        if(nSections >= "1") {
            theTileMap += "<td $theStyle>"
            if(wordsBEF) makeTileLine(theDevice,wordsBEF,linkBEF)
            if(wordsBEF) theTileMap += "${newWords2}"
		    if(deviceAtts && !hideAttr) theTileMap += "${deviceStatus}"
		    if(wordsAFT) makeTileLine(theDevice,wordsAFT,linkAFT)
            if(wordsAFT) theTileMap += "${newWords2}"
            theTileMap += "</td>"
    	} 
        if(nSections >= "2") {
            theTileMap += "<td $theStylea>"
            if(wordsBEFa) makeTileLine(theDevicea,wordsBEFa,linkBEFa)
            if(wordsBEFa) theTileMap += "${newWords2}"
		    if(deviceAttsa && !hideAttra) theTileMap += "${deviceStatusa}"
		    if(wordsAFTa) makeTileLine(theDevicea,wordsAFTa,linkAFTa)
            if(wordsAFTa) theTileMap += "${newWords2}"
            theTileMap += "</td>"
    	}
        if(nSections == "3") {
            theTileMap += "<td $theStyleb>"
            if(wordsBEFb) makeTileLine(theDeviceb,wordsBEFb,linkBEFb)
            if(wordsBEFb) theTileMap += "${newWords2}"
		    if(deviceAttsb && !hideAttrb) theTileMap += "${deviceStatusb}"
		    if(wordsAFTb) makeTileLine(theDeviceb,wordsAFTb,linkAFTb)
            if(wordsAFTb) theTileMap += "${newWords2}"
            theTileMap += "</td>"
    	}
    
    	theTileMap += "</tr></table>"
    
        if(x == 1) {
            state.theTile_1 = theTileMap
            state.theTileLength_1 = theTileMap.length()
        }
        if(x == 2) {
            state.theTile_2 = theTileMap
            state.theTileLength_2 = theTileMap.length()
        }
        if(x == 3) {
            state.theTile_3 = theTileMap
            state.theTileLength_3 = theTileMap.length()
        }
        if(x == 4) {
            state.theTile_4 = theTileMap
            state.theTileLength_4 = theTileMap.length()
        }
        if(x == 5) {
            state.theTile_5 = theTileMap
            state.theTileLength_5 = theTileMap.length()
        }
        if(x == 6) {
            state.theTile_6 = theTileMap
            state.theTileLength_6 = theTileMap.length()
        }
        if(x == 7) {
            state.theTile_7 = theTileMap
            state.theTileLength_7 = theTileMap.length()
        }
        if(x == 8) {
            state.theTile_8 = theTileMap
            state.theTileLength_8 = theTileMap.length()
        }
        if(x == 9) {
            state.theTile_9 = theTileMap
            state.theTileLength_9 = theTileMap.length()
        }
        
        if(logEnable) log.debug "In tileHandler - Line: ${x} - theTileMap: ${theTileMap} - theTileLength: ${theTileLength}"
    }
}

def sampleTileHandler(evt){
	if(logEnable) log.debug "In sampleTileHandler (${state.version})"
    tileHandler()
    
	section(getFormat("header-green", "${getImage("Blank")}"+" Sample Tile")) {
        paragraph "For testing purposes only"
        input "bgColor", "text", title: "Background Color (ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White)", required: false, submitOnChange: true, width: 6
        input "tableWidth", "number", title: "Table Width (1 - 900)", description: "1-900", required: false, defaultValue: "300", submitOnChange: true, width: 6
        
        makeTile()
        paragraph "<table style='width:${tableWidth}px;background-color:${bgColor};border:1px solid grey'><tr><td>${tileData}</td></tr></table>"
        
        try {
            totalLength = 45
            if(state.theTile_1) totalLength = totalLength + state.theTileLength_1
            if(state.theTile_2) totalLength = totalLength + state.theTileLength_2
            if(state.theTile_3) totalLength = totalLength + state.theTileLength_3
            if(state.theTile_4) totalLength = totalLength + state.theTileLength_4
            if(state.theTile_5) totalLength = totalLength + state.theTileLength_5
            if(state.theTile_6) totalLength = totalLength + state.theTileLength_6
            if(state.theTile_7) totalLength = totalLength + state.theTileLength_7
            if(state.theTile_8) totalLength = totalLength + state.theTileLength_8
            if(state.theTile_9) totalLength = totalLength + state.theTileLength_9
        } catch(e) {
            log.error "Tile Master - Something went wrong. ${e}"
        }

        parag = "Characters - "
        if(state.theTile_1) parag += "Line 1: ${state.theTileLength_1} - "
        if(state.theTile_2) parag += "Line 2: ${state.theTileLength_2} - "
        if(state.theTile_3) parag += "Line 3: ${state.theTileLength_3} - "
        if(state.theTile_4) parag += "Line 4: ${state.theTileLength_4} - "
        if(state.theTile_5) parag += "Line 5: ${state.theTileLength_5} - "
        if(state.theTile_6) parag += "Line 6: ${state.theTileLength_6} - "
        if(state.theTile_7) parag += "Line 7: ${state.theTileLength_7} - "
        if(state.theTile_8) parag += "Line 8: ${state.theTileLength_8} - "
        if(state.theTile_9) parag += "Line 9: ${state.theTileLength_9} - "
        if(logEnable) log.debug "${parag}"
        paragraph "<hr>"
        paragraph "${parag}<br>* This is only an estimate. Actual character count can be found in the tile device."
		if(totalLength <= 1024) {
			paragraph "Total Number of Characters: <font color='green'>${totalLength}</font><br><small>* Must stay under 1024 to display on Dashboard.<br>* Count includes all html characters needed to format the tile.</small>"
		} else {
			paragraph "Total Number of Characters: <font color='red'>${totalLength}<br><small>* Must stay under 1024 to display on Dashboard.<br>* Count includes all html characters needed to format the tile.</small></font>"
		}
	}
}

def makeTile() {
    if(logEnable) log.debug "In makeTile - *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"
    if(logEnable) log.debug "In makeTile (${state.version}) - howManyLines: ${howManyLines}"
    tileData = "<table width=100%><tr><td>"
    
    if(state.theTile_1) tileData += state.theTile_1
    if(state.theTile_2) tileData += state.theTile_2
    if(state.theTile_3) tileData += state.theTile_3
    if(state.theTile_4) tileData += state.theTile_4
    if(state.theTile_5) tileData += state.theTile_5
    if(state.theTile_6) tileData += state.theTile_6
    if(state.theTile_7) tileData += state.theTile_7
    if(state.theTile_8) tileData += state.theTile_8
    if(state.theTile_9) tileData += state.theTile_9

    tileData += "</td></tr></table>"
    
    if(logEnable) log.debug "In makeTile - tileData: ${tileData}"
    if(tileDevice) {
        tileDevice.sendTile01(tileData)
        if(logEnable) log.debug "In makeTile - tileData sent"
    }
    if(logEnable) log.debug "In makeTile - *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"
    return tileData
}

def getStatusColors(deviceStatus,deviceAtts) {
    if(logEnable) log.debug "In getStatusColors (${state.version}) - Received: ${deviceAtts} - ${deviceStatus}"
    
    if(deviceAtts) {
        if(deviceAtts.toLowerCase() == "temperature") {
            tempLow = parent.tempLow.toInteger()
            tempHigh = parent.tempHigh.toInteger()
            if(deviceStatus <= tempLow) deviceStatus1 = "<span style='color:${parent.colorTempLow}'>${deviceStatus}</span>"
            if(deviceStatus > tempLow && deviceStatus < tempHigh) deviceStatus1 = "<span style='color:${parent.colorTemp}'>${deviceStatus}</span>"
            if(deviceStatus >= tempHigh) deviceStatus1 = "<span style='color:${parent.colorTempHigh}'>${deviceStatus}</span>"
        }
    
        if(deviceAtts.toLowerCase() == "battery") {
            battLow = parent.battLow.toInteger()
            battHigh = parent.battHigh.toInteger()
            if(deviceStatus <= battLow) deviceStatus1 = "<span style='color:${parent.colorBattLow}'>${deviceStatus}</span>"
            if(deviceStatus > battLow && deviceStatus < battHigh) deviceStatus1 = "<span style='color:${parent.colorBatt}'>${deviceStatus}</span>"
            if(deviceStatus >= battHigh) deviceStatus1 = "<span style='color:${parent.colorBattHigh}'>${deviceStatus}</span>"
        }
    }
    
    if(deviceStatus == "on") deviceStatus1 = "<span style='color:${parent.colorOn}'>on</span>"
    if(deviceStatus == "off") deviceStatus1 = "<span style='color:${parent.colorOff}'>off</span>"
    
    if(deviceStatus == "open") deviceStatus1 = "<span style='color:${parent.colorOpen}'>open</span>"
    if(deviceStatus == "closed") deviceStatus1 = "<span style='color:${parent.colorClosed}'>closed</span>"
    
    if(deviceStatus == "active") deviceStatus1 = "<span style='color:${parent.colorActive}'>active</span>"
    if(deviceStatus == "inactive") deviceStatus1 = "<span style='color:${parent.colorInactive}'>inactive</span>"

    if(deviceStatus == "locked") deviceStatus1 = "<span style='color:${parent.colorLocked}'>locked</span>"
    if(deviceStatus == "unlocked") deviceStatus1 = "<span style='color:${parent.colorUnlocked}'>unlocked</span>"
    
    if(deviceStatus == "wet") deviceStatus1 = "<span style='color:${parent.colorWet}'>wet</span>"
    if(deviceStatus == "dry") deviceStatus1 = "<span style='color:${parent.colorDry}'>dry</span>"
    
    if(deviceStatus == "present") deviceStatus1 = "<span style='color:${parent.colorPresent}'>present</span>"
    if(deviceStatus == "not present") deviceStatus1 = "<span style='color:${parent.colorNotPresent}'>not present</span>"

    if(deviceStatus == "clear") deviceStatus1 = "<span style='color:${parent.colorClear}'>clear</span>"
    if(deviceStatus == "detected") deviceStatus1 = "<span style='color:${parent.colorDetected}'>detected</span>"
  
    if(deviceStatus1 == null) deviceStatus1 = deviceStatus
    if(logEnable) log.debug "In getStatusColors - Returning: ${deviceStatus1}"
    return deviceStatus1
}

def getCellColors(deviceStatus,deviceAtts) {
    if(logEnable) log.debug "In getCellColors (${state.version}) - Received: ${deviceAtts} - ${deviceStatus}"
    
    if(deviceAtts) {
        if(deviceAtts.toLowerCase() == "temperature") {
            tempLow = parent.tempLow.toInteger()
            tempHigh = parent.tempHigh.toInteger()
            if(deviceStatus <= tempLow) cellColor = "${parent.colorTempLow}"
            if(deviceStatus > tempLow && cellColor < tempHigh) deviceStatus1 = "${parent.colorTemp}"
            if(deviceStatus >= tempHigh) cellColor = "${parent.colorTempHigh}"
        }
    
        if(deviceAtts.toLowerCase() == "battery") {
            battLow = parent.battLow.toInteger()
            battHigh = parent.battHigh.toInteger()
            if(deviceStatus <= battLow) cellColor = "${parent.colorBattLow}"
            if(deviceStatus > battLow && cellColor < battHigh) deviceStatus1 = "${parent.colorBatt}"
            if(deviceStatus >= battHigh) cellColor = "${parent.colorBattHigh}"
        }
    }
    
    if(deviceStatus == "on") theCellColor = "${parent.colorOn}"
    if(deviceStatus == "off") theCellColor = "${parent.colorOff}"
    
    if(deviceStatus == "open") theCellColor = "${parent.colorOpen}"
    if(deviceStatus == "closed") theCellColor = "${parent.colorClosed}"
    
    if(deviceStatus == "active") theCellColor = "${parent.colorActive}"
    if(deviceStatus == "inactive") theCellColor = "${parent.colorInactive}"

    if(deviceStatus == "locked") theCellColor = "${parent.colorLocked}"
    if(deviceStatus == "unlocked") theCellColor = "${parent.colorUnlocked}"
    
    if(deviceStatus == "wet") theCellColor = "${parent.colorWet}"
    if(deviceStatus == "dry") theCellColor = "${parent.colorDry}"
    
    if(deviceStatus == "present") theCellColor = "${parent.colorPresent}"
    if(deviceStatus == "not present") theCellColor = "${parent.colorNotPresent}"

    if(deviceStatus == "clear") theCellColor = "${parent.colorClear}"
    if(deviceStatus == "detected") theCellColor = "${parent.colorDetected}"
  
    if(logEnable) log.debug "In getCellColors - Returning: ${theCellColor}"
    return theCellColor
}

def makeTileLine(theDevice,words,linkName) {
    if(logEnable) log.debug "In makeTileLine (${state.version}) - device: ${theDevice} - words: ${words} - linkName: ${linkName} - dateTimeFormat: ${dateTimeFormat}"
    if(words.toLowerCase().contains("wlink")) { 
        newWords = words.toLowerCase()
        if(logEnable) log.debug "In makeTileLine - newWords contains wlink"
        newWords = newWords.replace("wlink","http")
        newWords2 = "<a href='${newWords}'>${linkName}</a>"
        if(logEnable) log.debug "In makeTileLine - newWords: ${newWords}"
    } else if(words.toLowerCase().contains("%lastact%")) {
        try {
            if(dateTimeFormat == "f1") dFormat = "MMM dd, yyy - h:mm:ss a"
            if(dateTimeFormat == "f2") dFormat = "dd MMM, yyy - h:mm:ss a"
            if(dateTimeFormat == "f3") dFormat = "MMM dd - h:mm:ss a"
            if(dateTimeFormat == "f4") dFormat = "dd MMM - h:mm:ss a"
            if(dateTimeFormat == "f5") dFormat = "MMM dd - HH:mm"
            if(dateTimeFormat == "f6") dFormat = "dd MMM - HH:mm"
            if(dateTimeFormat == "f7") dFormat = "h:mm:ss a"
            if(dateTimeFormat == "f8") dFormat = "HH:mm:ss"
            
            lAct = theDevice.getLastActivity().format("${dFormat}")
            
            if(logEnable) log.debug "In makeTileLine - lAct: ${lAct}"
            if(lAct) {
                newWords2 = words.replace("%lastAct%","${lAct}")
            } else {
                newWords2 = words.replace("%lastAct%","Not Available")
            }
        } catch (e) {
            newWords2 = words.replace("%lastAct%","Not Available")
            log.error e
        }
    } else {
        newWords2 = "${words}"
    }
    if(logEnable) log.debug "In makeTileLine - newWords2: ${newWords2}"
    return newWords2
}

def createChildDevice() {    
    if(logEnable) log.debug "In createChildDevice (${state.version})"
    statusMessage = ""
    if(!getChildDevice("TM - " + userName)) {
        if(logEnable) log.warn "In createChildDevice - Child device not found - Creating device Location Tracker - ${userName}"
        try {
            addChildDevice("BPTWorld", "Tile Master Driver", "TM - " + userName, 1234, ["name": "TM - ${userName}", isComponent: false])
            if(logEnable) log.debug "In createChildDevice - Child device has been created! (TM - ${userName})"
            statusMessage = "<b>Device has been been created. (TM - ${userName})</b>"
        } catch (e) { log.warn "Tile Master unable to create device - ${e}" }
    } else {
        statusMessage = "<b>Device Name (TM - ${userName}) already exists.</b>"
    }
    return statusMessage
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
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
    section (getFormat("title", "${getImage("logo")}" + " Tile Master 2 - ${theName}")) {
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
