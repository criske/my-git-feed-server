
# My Git Feed
REST API backend app that centralize my git activity on various git platform providers: Github, Gitlab, Bitbucket.


### Endpoints
- `/api/{provider}` (all besides `/me` can have an optional `page={no}` query paramater )
  - `/assignments?state={all|open|closed}`
  - `/commits`
  - `/me`
  - `/repos`
- `/check` (api status checks)
  - `/ping` 
  
### Miscellaneous
- `/actuator/httptrace` - http trace actuator endpoint<sup>*</sup>
- `/httptrace` - ui for http trace actuator endpoint<sup>*</sup>
![httptrace](https://user-images.githubusercontent.com/10284893/125584453-c4031a95-6712-45da-8b00-d0e28f008c53.png)

<sub><sup>*requires authentication</sup></sub>
