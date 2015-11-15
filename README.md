# regserver
A simple RESTful API Server

The interface is a follows:

*v1/users:

```
POST, JSON:
{
"userName":"",
"password":""
}
Response:
203 - Created
"location" header:
v1/users/<UUID of user>
```

*v1/users/login

```
POST, JSON:
{
"uuid":<string>,
"userName":<string>,
"password":<string>
}
Where "uuid" or "userName" nedds to be set

Response:
200 - OK
or
401 - UNAUTHORIZED
```

*v1/users/getlogins

```
POST, JSON:
{
"numLogs":<numeric>,
"credentials":{
	"uuid":<string>,
	"password":<string>
	}
}
Reponse:
200 - OK, JSON:
{
"user":<string>,
logins:<string>[]
}
or 401 - UNAUTHORIZED
```

