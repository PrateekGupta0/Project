LinkedIn Profile API

A Spring Boot REST API that accepts a LinkedIn profile URL and returns structured profile information such as:

Name

Headline

Location

About

Experience

Education

Skills

Certifications

Languages

Profile images

Source/retrieval metadata

The project is designed around LinkedIn's current profile-page architecture, where some profile sections can be rendered directly in the initial HTML while other sections may be loaded lazily through LinkedIn's SDUI/RSC component endpoints.

Important: This project is intended for authorized/legitimate use of LinkedIn data. It forwards the caller's LinkedIn session cookie to LinkedIn. Use it only where you have the right to access the profile and comply with LinkedIn's terms, applicable laws, and organizational policies.

Features

1. REST API

Supports both:

GET /api/v1/profile

POST /api/v1/profile

The API validates that the supplied URL is a LinkedIn /in/ profile URL.

2. LinkedIn HTML parsing

The application fetches the profile page and uses Jsoup to parse the returned HTML.

The parser attempts to extract:

Basic profile information

About section

Experience

Education

Skills

Certifications

Languages

Profile image URLs

The parser intentionally avoids depending heavily on LinkedIn-generated CSS class names.

3. Lazy-loaded component discovery

LinkedIn can return SDUI metadata in the initial page response that identifies replaceable components.

LinkedInComponentExtractor looks for component requests such as:

com.linkedin.sdui.generated.profile.dsl.impl.profileCardsExperienceOnly

and extracts:

component ID

LinkedIn vanity name

parent span/component identifier

The application can then call LinkedIn's component endpoint to retrieve the corresponding lazy-loaded response.

4. Cookie forwarding

The incoming HTTP Cookie header is forwarded to the LinkedIn request.

This is required for pages/components whose content is only available to an authenticated LinkedIn session.

Example:

Cookie: li_at=YOUR_SESSION_COOKIE; JSESSIONID=YOUR_SESSION_ID

Do not hard-code cookies in source code.

5. Validation and error handling

The API provides centralized exception handling through GlobalExceptionHandler.

Handled cases include:

Invalid profile URL → 400 Bad Request

Missing profile → 404 Not Found

Upstream provider failure → 502 Bad Gateway

Unexpected server error → 500 Internal Server Error

6. Swagger / OpenAPI

Springdoc OpenAPI is included for API documentation.

Swagger UI:

http://localhost:8080/docs

7. Health endpoint

Spring Boot Actuator exposes health information:

http://localhost:8080/actuator/health

Architecture

The application follows a simple layered architecture:

Client
|
v
ProfileController
|
v
ProfileService
|
v
ProfileDataProvider
|
+------------------------------+
|                              |
v                              v
LinkedIn profile HTML       Lazy-loaded SDUI component
|                              |
v                              v
ProfileParser             LinkedInComponentExtractor
|                              |
+---------------+--------------+
|
v
ProfileResponse

Main components

Component

Responsibility

ProfileController

Exposes GET/POST profile endpoints

ProfileService

Normalizes URLs and delegates profile retrieval

ProfileDataProvider

Abstraction for the upstream profile data source

StubProfileDataProvider

Current LinkedIn HTTP implementation

ProfileParser

Parses profile HTML using Jsoup

LinkedInComponentExtractor

Detects lazy-loaded SDUI components

ExperienceParser

Extension point for parsing lazy-loaded experience responses

EducationParser

Extension point for parsing lazy-loaded education responses

SectionParser

Common parser interface for profile sections

GlobalExceptionHandler

Converts exceptions into consistent API responses

Technology Stack

Java 17

Spring Boot 3.3.2

Spring Web

Spring Validation

Spring Cache

Caffeine

Jsoup

Lombok

Springdoc OpenAPI / Swagger

Spring Boot Actuator

Maven

Docker

Project Structure

linkedin-profile-api/
└── linkedin-profile-api/
├── pom.xml
├── Dockerfile
├── README.md
└── src/
├── main/
│   ├── java/
│   │   └── com/tross/linkedinprofileapi/
│   │       ├── LinkedInProfileApiApplication.java
│   │       ├── controller/
│   │       │   └── ProfileController.java
│   │       ├── dto/
│   │       │   ├── ProfileRequest.java
│   │       │   └── ProfileResponse.java
│   │       ├── exception/
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ProfileNotFoundException.java
│   │       │   └── UpstreamServiceException.java
│   │       └── service/
│   │           ├── EducationParser.java
│   │           ├── ExperienceParser.java
│   │           ├── LinkedInComponentExtractor.java
│   │           ├── ProfileDataProvider.java
│   │           ├── ProfileParser.java
│   │           ├── ProfileService.java
│   │           ├── SectionParser.java
│   │           └── StubProfileDataProvider.java
│   └── resources/
│       └── application.yml
└── test/
└── java/
└── com/tross/linkedinprofileapi/
└── controller/
└── ProfileControllerTest.java

Prerequisites

Install:

JDK 17+

Maven 3.9+

Docker (optional)

Verify Java:

java -version

Verify Maven:

mvn -version

Running Locally

Go to the Maven project directory:

cd linkedin-profile-api/linkedin-profile-api

Build:

mvn clean package

Run:

mvn spring-boot:run

The application starts on:

http://localhost:8080

The port can be changed using:

PORT=9090

On Windows PowerShell:

$env:PORT=9090
mvn spring-boot:run

API Usage

GET profile

Request:

GET /api/v1/profile?url=https://www.linkedin.com/in/example-user

If the upstream LinkedIn request requires an authenticated session, forward the cookie:

curl "http://localhost:8080/api/v1/profile?url=https://www.linkedin.com/in/example-user" \
-H "Cookie: li_at=YOUR_SESSION_COOKIE; JSESSIONID=YOUR_SESSION_ID"

POST profile

Request:

POST /api/v1/profile
Content-Type: application/json

Body:

{
"profileUrl": "https://www.linkedin.com/in/example-user"
}

With a cookie:

curl -X POST "http://localhost:8080/api/v1/profile" \
-H "Content-Type: application/json" \
-H "Cookie: li_at=YOUR_SESSION_COOKIE; JSESSIONID=YOUR_SESSION_ID" \
-d "{\"profileUrl\":\"https://www.linkedin.com/in/example-user\"}"

Response Model

A successful response follows this structure:

{
"profileUrl": "https://www.linkedin.com/in/example-user",
"name": "Example User",
"headline": "Software Engineer",
"location": "India",
"about": "Software engineer with experience building backend systems.",
"experience": [
{
"title": "Software Engineer",
"company": "Example Company",
"employmentType": "Full-time",
"dateRange": "2024 - Present",
"location": "India",
"description": "Building backend services."
}
],
"education": [
{
"institution": "Example University",
"degree": "Bachelor of Technology",
"fieldOfStudy": "Computer Science",
"dateRange": "2020 - 2024",
"description": "Computer Science and Engineering"
}
],
"skills": [
"Java",
"Spring Boot",
"Kafka"
],
"certifications": [
{
"name": "Example Certification",
"issuingOrganization": "Example Organization",
"issueDate": "2025"
}
],
"languages": [
{
"name": "English",
"proficiency": "Professional"
}
],
"profileImages": [
"https://media.licdn.com/..."
],
"meta": {
"source": "linkedin",
"retrievedAt": "2026-08-30T12:00:00Z",
"cached": false
}
}

Fields that cannot be extracted are omitted because ProfileResponse uses JsonInclude.Include.NON_NULL.

How Lazy Loading Works

A LinkedIn profile page does not necessarily contain every profile section in the initial HTML.

A typical flow is:

1. GET LinkedIn profile page
   |
   v
2. Receive initial HTML / RSC / SDUI data
   |
   v
3. Inspect SDUI component metadata
   |
   v
4. Find replaceable/lazy-loaded components
   |
   v
5. Extract componentId + parentSpanId + vanityName
   |
   v
6. Call component endpoint
   |
   v
7. Receive RSC/SDUI response
   |
   v
8. Parse the response
   |
   v
9. Merge data into ProfileResponse

For example, the project detects the experience component:

com.linkedin.sdui.generated.profile.dsl.impl.profileCardsExperienceOnly

The component request is constructed using the component ID, vanity name and parent span ID.

RSC / SDUI Responses

Some LinkedIn responses are not normal JSON objects. They may be returned as RSC-style payloads or escaped/serialized data.

For example, a response may contain structures similar to:

{"data": ...}
{"included": [...]}

or streamed/encoded content containing:

$recipeTypes
$type
componentId
componentKey
newComponentId
asyncContent

The important point is that the lazy-loaded response should not be treated as ordinary profile HTML.

The intended parsing pipeline is:

Raw RSC response
|
v
Decode / normalize response
|
v
Locate component payload
|
v
Extract profile section data
|
v
Experience / Education / etc.
|
v
ProfileResponse

The ExperienceParser and EducationParser classes are already provided as extension points for this part of the implementation.

Configuration

src/main/resources/application.yml contains:

server:
port: ${PORT:8080}

spring:
application:
name: linkedin-profile-api
cache:
cache-names: profiles
caffeine:
spec: maximumSize=500,expireAfterWrite=1h

profile:
data-source: ${PROFILE_DATA_SOURCE:stub}
provider:
base-url: ${PROVIDER_BASE_URL:}
api-key: ${PROVIDER_API_KEY:}

management:
endpoints:
web:
exposure:
include: health,info

springdoc:
swagger-ui:
path: /docs

Environment variables

Variable

Description

Default

PORT

HTTP server port

8080

PROFILE_DATA_SOURCE

Data provider selection

stub

PROVIDER_BASE_URL

Optional external provider URL

empty

PROVIDER_API_KEY

Optional external provider API key

empty

Caching

Caffeine is included and configured for:

Maximum entries: 500
Expiration: 1 hour

The application enables Spring caching with:

@EnableCaching

The current ProfileService does not yet apply @Cacheable to getProfile(). If caching is enabled for production, the cache key should be designed carefully.

In particular, avoid using the raw LinkedIn session cookie as a shared cache key or storing authenticated responses where they could be exposed to another user.

Error Response

Errors are returned in a consistent format:

{
"timestamp": "2026-08-30T12:00:00Z",
"status": 400,
"error": "Bad Request",
"message": "profileUrl must be a valid LinkedIn profile URL"
}

Docker

Build the application:

mvn clean package

Build the Docker image:

docker build -t linkedin-profile-api .

Run:

docker run -p 8080:8080 linkedin-profile-api

Then access:

http://localhost:8080

Testing

Run:

mvn test

The project contains controller-level test scaffolding under:

src/test/java/com/tross/linkedinprofileapi/controller/

Before using the tests as a CI gate, ensure the expected response assertions match the current upstream provider/parser implementation.

Design Decisions

Provider abstraction

The upstream data source is represented by:

public interface ProfileDataProvider {
ProfileResponse fetchProfile(String profileUrl, String cookie);
}

This keeps the controller and service independent of the actual data provider.

It allows the implementation to be replaced later with:

A licensed enrichment provider

A mock provider

A test fixture provider

Another authorized upstream source

without changing the public API.

Parser abstraction

Lazy-loaded section parsers implement:

public interface SectionParser<T> {
boolean supports(String componentId);
List<T> parse(String response);
}

This makes it possible to add parsers for:

Experience
Education
Skills
Certifications
Languages

independently.

Current Implementation Status

The project currently has two complementary parsing paths:

Initial HTML parsing

Implemented in ProfileParser:

Name

Headline

Location

Profile URL

Profile images

About

Experience

Education

Skills

Certifications

Languages

Lazy-loaded section retrieval

Component discovery is implemented in:

LinkedInComponentExtractor

and the application can make the component request through:

StubProfileDataProvider#getComponent(...)

The following classes are currently extension points and return empty lists:

ExperienceParser
EducationParser

Therefore, the next implementation step for complete lazy-loaded profile extraction is to decode the captured RSC/SDUI component response and populate these parsers, then merge their results with the initial ProfileResponse.

Important Production Considerations

LinkedIn's internal HTML, SDUI and RSC structures are implementation details and can change without notice.

Avoid relying on:

Generated CSS class names

Fixed array positions

Exact response formatting

Undocumented internal endpoints

Hard-coded component identifiers without fallback handling

A production implementation should also add:

Request timeouts

Retry/backoff for transient upstream failures

Connection pooling

Structured logging

Response-size limits

Rate limiting

Circuit breaking

Better upstream status handling

Tests using representative captured fixtures

Parser fallbacks when LinkedIn changes its payload structure

Secure handling/redaction of session cookies

Security

Never log session cookies

Do not use:

System.out.println("Cookie: " + cookie);

in production.

The current implementation contains this statement for development/debugging and it should be removed or replaced with redacted logging before deployment.

Do not commit credentials

Never commit:

li_at
JSESSIONID
PROVIDER_API_KEY

to Git.

Use environment variables or a secrets manager.

HTTPS

Deploy the API behind HTTPS when handling authenticated requests.

API Endpoints Summary

Method

Endpoint

Description

GET

/api/v1/profile?url=...

Fetch profile using query parameter

POST

/api/v1/profile

Fetch profile using JSON body

GET

/actuator/health

Application health

GET

/docs

Swagger UI

Example Development Flow

Client
|
| GET /api/v1/profile?url=...
| Cookie: LinkedIn session
v
ProfileController
|
v
ProfileService
|
v
StubProfileDataProvider
|
|-------------------- GET profile URL
|                              |
|                              v
|                       LinkedIn HTML
|                              |
|                              v
|                     ProfileParser
|
|-------------------- Inspect SDUI
|
v
LinkedInComponentExtractor
|
v
componentId / parentSpanId
|
v
Component endpoint
|
v
RSC response
|
v
ExperienceParser / EducationParser
|
v
ProfileResponse
|
v
Client

Future Improvements

Implement robust RSC response decoding.

Fully parse lazy-loaded Experience data.

Fully parse lazy-loaded Education data.

Add additional section parsers.

Merge duplicate entries from initial HTML and lazy-loaded responses.

Add fixture-based tests for captured RSC responses.

Add request timeout and retry policies.

Add circuit breaker protection around upstream calls.

Add structured/redacted logging.

Add production-grade caching with safe cache keys.

Add integration tests for the complete HTML → SDUI → response pipeline.

License

This project is provided for educational/development purposes. Before using it against LinkedIn or any other third-party service, ensure that your use complies with the service's terms, authorization requirements, privacy obligations, and applicable laws.