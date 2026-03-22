# Image Processing Service

Build a service that allows users to upload and process images.

Project
URL: <a href="https://roadmap.sh/projects/image-processing-service">https://roadmap.sh/projects/image-processing-service</a>

## Tech Stack

- Backend Framework: Spring Boot
- Authentication: Spring Security + JWT
- Database: PostgreSQL + Spring Data JPA
- Cache: Redis
- Cloud Storage: AWS S3 / Cloudflare R2
- Image Processing: Thumbnailator / imgscalr / ImageMagick
- Rate Limit: Bucket4j Core

## Requirements

Here is the list of features that you should implement in this project:

### User Authentication

- Sign-Up: Allow users to create an account.
- Log-In: Allow users to log into their account.
- JWT Authentication: Secure endpoints using JWTs for authenticated access.

### Image Management

- Upload Image: Allow users to upload images.
- Transform Image: Allow users to perform various transformations (resize, crop, rotate, watermark etc.).
- Retrieve Image: Allow users to retrieve a saved image in different formats.
- List Images: List all uploaded images by the user with metadata.

### Image Transformation

Here is the list of transformations that you can implement:

- Resize
- Crop
- Rotate
- Watermark
- Flip
- Mirror
- Compress
- Change format (JPEG, PNG, etc.)
- Apply filters (grayscale, sepia, etc.)

Feel free to add more transformations based on your interest and expertise.

## API List

Here is the list of endpoints that you can implement for this project:

### Authentication Endpoints

#### 1. Register a new user:

```text
POST /register
{
    "username": "user1",
    "password": "password123"
}
```

Response should be the user object with a JWT.

#### 2. Log in an existing user:

```text
POST /login
{
  "username": "user1",
  "password": "password123"
}
```

Response should be the user object with a JWT.

### Image Management Endpoints

#### 1. Upload an image:

```text
POST /images
Request Body: Multipart form-data with image file
Response: Uploaded image details (URL, metadata).
```

#### 2. Apply transformations to an image:

```text
POST /images/:id/transform
{
    "transformations": {
        "resize": {
        "width": "number",
        "height": "number"
        },
        "crop": {
            "width": "number",
            "height": "number",
            "x": "number",
            "y": "number"
        },
        "rotate": "number",
        "format": "string",
        "filters": {
            "grayscale": "boolean",
            "sepia": "boolean"
        }
    }
}
```

User can apply one or more transformations to the image. Response should be the transformed image details (URL,
metadata).

#### 3. Retrieve an image:

```text
GET /images/:id
```

Response should be the image actual image detail.

#### 4. Get a paginated list of images:

```text
GET /images?page=1&limit=10
```
