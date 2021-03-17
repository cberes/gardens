# front-end

## Configuration

These environment variables must be defined when building for production

- `API_URL`: base URL for API endpoints, e.g. `https://xyz123.execute-api.us-east-1.amazonaws.com/v1`
- `AWS_REGION`: AWS region, e.g. `us-east-1`
- `AWS_USER_POOL_ID`: Cognito user pool ID
- `AWS_USER_POOL_CLIENT_ID`: Cognito user pool client ID

Additionally these optional environment variables may be defined

- `BASE_URL`: base URL for assets, e.g. `app/`

A script is included that will `export` the specified environment's configuration
```
. ./setup-env <environment>
```

## Compile with hot-reload for development
```
gradle clean run
```

## Compile and minify for production
```
gradle clean build
```

## Update static website
A script is included that will update the static website's contents.
```
./update-site <environment>
```

It assumes there is a file containing all relevant environment variables at `~/.gardens/<environment>.conf`. For example, this file will be at ~/.gardens/live.conf for the `live` environment.
