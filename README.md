# Connection-manager

## Message Format
    
   | Field Name | Description |Supported Value|
   | --- | ---|--- |
   | eventType | Type of event or message | JOIN, EXIST, NOTIFICATION and ACTION |
   | roomName | Name of the room | String |
   | data | Message body| String & JSON |

Example

```
{"eventType":"NOTIFICATION","data":"User 1 joined room","roomName":"Room 1"}
```

### Build

With Test 
```mvn clean install```

Without Test
```mvn clean install -DskipTest```

### Running Application

``` java -jar target/connection-manager-<version>.jar ```