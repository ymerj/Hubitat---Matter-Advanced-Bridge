/* groovylint-disable CompileStatic, DuplicateStringLiteral, LineLength, PublicMethodsBeforeNonPublicMethods */
/*
  *  'Matter Generic Component Motion Sensor' - component driver for Matter Advanced Bridge
  *
  *  https://community.hubitat.com/t/dynamic-capabilities-commands-and-attributes-for-drivers/98342
  *  https://community.hubitat.com/t/project-zemismart-m1-matter-bridge-for-tuya-zigbee-devices-matter/127009
  *
  *  Licensed Virtual the Apache License, Version 2.0 (the "License"); you may not use this file except
  *  in compliance with the License. You may obtain a copy of the License at:
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
  *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
  *  for the specific language governing permissions and limitations under the License.
  *
  * ver. 1.0.0  2024-03-16 kkossev  - first release
  *
*/

import groovy.transform.Field

@Field static final String matterComponentSwitchVersion = '1.0.0'
@Field static final String matterComponentSwitchStamp   = '2024/03/16 9:26 AM'

metadata {
    definition(name: 'Matter Generic Component Event', namespace: 'kkossev', author: 'Krassimir Kossev', importUrl: '') {

        capability 'Refresh'
        capability "PushableButton"
        //capability 'Health Check'       // Commands:[ping]

        //attribute 'healthStatus', 'enum', ['unknown', 'offline', 'online']

        attribute 'currentPosition', 'string'
    }
}

preferences {
    section {
        input name: 'logEnable',
              type: 'bool',
              title: 'Enable debug logging',
              required: false,
              defaultValue: true

        input name: 'txtEnable',
              type: 'bool',
              title: 'Enable descriptionText logging',
              required: false,
              defaultValue: true
    }
}

/* groovylint-disable-next-line UnusedMethodParameter */
void parse(String description) { log.warn 'parse(String description) not implemented' }

// parse commands from parent
void parse(List<Map> description) {
    //if (logEnable) { log.debug "${description}" }
    description.each { d ->
        if (d.name == 'currentPosition') {
            if (device.currentValue('currentPosition') != d.value) {
                if (d.descriptionText && txtEnable) { log.info "${d.descriptionText} (value changed)" }
                sendEvent(d)
                sendEvent(name: "pushed", value: 1, descriptionText: "button was pushed", isStateChange:true)
                runIn(3, "delaySend")
            }
            else {
                if (logEnable) { log.debug "${device.displayName} : ignored event '${d.value}' (no change)" }
            }
        }
        else {
            if (d.descriptionText && txtEnable) { log.info "${d.descriptionText}" }
            sendEvent(d)
        }
    }

}

def delaySend(){
    if (txtEnable) log.info "currentPosition : 00"
    sendEvent(name: "currentPosition", value: "00")
}

void push(bn = 1) {
    log.warn "Button not pushable"
}

// Component command to ping the device
void ping() {
    parent?.componentPing(device)
}

// Called when the device is first created
void installed() {
    log.info "${device.displayName} driver installed"
}

// Called when the device is removed
void uninstalled() {
    log.info "${device.displayName} driver uninstalled"
}

// Called when the settings are updated
void updated() {
    log.info "${device.displayName} driver configuration updated"
    sendEvent(name: "numberOfButtons", value: 1)
    if (logEnable) {
        log.debug settings
        runIn(86400, 'logsOff')

    }
}

/* groovylint-disable-next-line UnusedPrivateMethod */
private void logsOff() {
    log.warn "debug logging disabled for ${device.displayName} "
    device.updateSetting('logEnable', [value: 'false', type: 'bool'] )
}

void refresh() {
    parent?.componentRefresh(this.device)
}

void setState(String stateName, String stateValue) {
    if (logEnable) { log.debug "${device.displayName} setting state '${stateName}' to '${stateValue}'" }
    state[stateName] = stateValue
}

String getState(String stateName) {
    if (logEnable) { log.debug "${device.displayName} getting state '${stateName}'" }
    return state[stateName]
}
