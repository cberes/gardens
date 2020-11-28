# front-end

## Configuration

These environment variables must be defined when building for production

- `API_URL`: base URL for API endpoints, e.g. `https://xyz123.execute-api.us-east-1.amazonaws.com/v1`
- `BASE_URL`: base URL for assets, e.g. `https://flowercompanion.com/`
- `AWS_REGION`: AWS region, e.g. `us-east-1`
- `AWS_USER_POOL_ID`: Cognito user pool ID
- `AWS_USER_POOL_CLIENT_ID`: Cognito user pool client ID

## Compile and minify for production
```
gradle clean build
```

## Compile with hot-reload for development
```
gradle clean run
```
