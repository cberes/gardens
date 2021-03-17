# gardens

Record all the plants that are in your gardens.

> Can't I just look at my garden?

Sure, of course, but maybe you forget the names of your plants.
You know something is a goldenrod, but what kind of goldenrod?
Also, you can use this app to remember seeds you've planted while you wait for their germination.

## Build the project
```
gradle clean build
```

## Deployment
### Front-end

The front-end is deployed automatically by the CI job, but there are also [instructions for manual deployment](front-end/README.md).

### Back-end

If you have not already, you will need to install and configure the [AWS CLI](https://aws.amazon.com/cli/).

Then run the provided script with your desired environment name as the only argument. For example:
```
./deploy-aws live
```

This is a manual step because we may not always want to deploy to AWS.

## History

This started as a [campsite manager using Spring Boot](https://github.com/cberes/campsites)
and then another attempt at a [campsite manager implemented as a serverless web app](https://github.com/cberes/ohboywerecamping).
Those projects proved to be too large for my rapidly diminishing spare time.
However, I wanted to complete a personal project with the tools I use professionally: Java, Javascript, Vue, Spring, and AWS to name a few.
So I'm implementing a much smaller web app that's like a slice of the original campsite manager.

## Design decisions

### Architecture

I chose to implement this as a serverless web app because I expect it will be used infrequently.
Days or months might go by without a single endpoint's being hit.
The serverless model seems counterintuitive for web apps, but in this case it's purely a cost issue.

### Language

Java is not ideal for Lambda functions due to its start-up delay.
However, it's the primary language that I use professionally.
If necessary, Graal can be used to create native binaries that start up quickly.

### Framework

I chose not to use a framework. In two of my last three jobs I used Spring.
Spring is a great tool, but I fear it would exacerbate Java's start-up costs.
Furthermore, dependency injection can be leveraged without Spring.

### Database

Dynamo is another tool I use professionally.
I also use relational databases such as MySQL (which I'm more comfortable with),
but I don't believe this was an option with the serverless architecture.
I believed it would be simplest to use all-Amazon infrastructure,
which would allow the app's deployment as a single Cloudformation app.
Dynamo's pricing is also a benefit as it is per-request. Redshift and RDS would require paying for a server
(and then the app wouldn't be serverless!).

### Module hierarchy

I divided the project into three modules

    Front-end -> Serverless-web-app -> Back-end

Keeping the AWS-specific code in the serverless-web-app module
would allow me to create an alternate interface, such as a Spring Boot application.
In that case the Lambda functions would be implemented as controllers and
repositories could be implemented using Hibernate entities.

### Package hierarchy

Rather than dividing code into a `services` package, a `controllers` package, etc, I used a hierarchy like
Robert C. Martin suggested in *Clean Architecture*. All plant code is in the `plant` package,
all garden code is in the `garden` package, etc. This organization keeps related code close.

### Front-end

I'll be honest, I consider myself more of a back-end person than a front-end person.
However, I believe every member of a team should be able to do any task that is assigned to the team,
and so I'm not afraid to work with the front-end.

I used Vue.js because I think a reactive framework makes front-end code much more intuitive.
In the past I've used Angular 1 and 2, but currently I work with Vue.js professionally.
