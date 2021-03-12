import json
json_string = '''
{
  "employees": [
    {
      "name": "John Watson",
      "dateOfJoin": "01/01/2018",
      "active": true,
      "awards": null
    },
    {
      "name": "William Ben",
      "dateOfJoin": "01/01/2015",
      "active": false,
      "awards": 1
    }
  ]
}
'''

id = 1
min = 100
json_string2="""[{"id":3, "min":31.364035,"max":52.709675,"avg":35.676388,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":4, "min":31.364035,"max":52.709675,"avg":35.676388,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":5, "min":31.364035,"max":52.709675,"avg":35.676388,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":6, "min":31.364035,"max":52.709675,"avg":35.676388,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":7, "min":39.735435,"max":46.9661,"avg":42.974174,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":8, "min":34.32007,"max":48.974125,"avg":41.27938,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":9, "min":30.629578,"max":84.53252,"avg":64.82596,"gps_longitude":22.155544,"gps_latitude":52.114014}{"id":10, "min":31.595673,"max":43.16725,"avg":34.07603,"gps_longitude":22.155579,"gps_latitude":52.113968}{"id":11, "min":28.943161,"max":64.87564,"avg":34.21918,"gps_longitude":22.155579,"gps_latitude":52.113968}]"""

employees_obj = json.loads(json_string2)
print(type(employees_obj))
#Print JSON Object
print("_______________Print Converted JSON_____________________")
print(employees_obj["id"])