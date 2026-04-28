Moxsend Processing Engine
---------------------------------------------------
A high-performance backend system designed to handle lead generation at scale. It takes a CSV file, processes each lead using AI, and generates personalized email copy in the background.
---------------------------------------------------
Live Demo
https://moxsend-engine.onrender.com
---------------------------------------------------
How it works
1. Upload: Send a CSV file to the system.
2. Instant Response: You immediately get a jobId so you don't have to wait for the processing to finish.
3. AI Processing: In the background, the engine uses Llama 3.1 to create a custom opening line and two catchy subject lines for every lead.
4. Fetch: Use your jobId to download the completed results once the status hits COMPLETED.

Tech Stack
- Language: Java 17
- Framework: Spring Boot
- Database: MongoDB Atlas (for job tracking and results)
- AI Core: Groq Cloud
- Deployment: Docker & Render

API Reference

1. Upload Leads
POST /api/upload
Upload your CSV via form-data using the key file.

Response:
{
  "jobId": "65f2a...",
  "status": "PENDING",
  "totalRows": 50
}

2. Check Status & Get Data
GET /api/result/{jobId}

Response:
{
  "status": "COMPLETED",
  "results": [
    {
      "name": "Ahmed",
      "openingLine": "Ahmed, noticed your work with Al Noor Logistics...",
      "subject1": "Future of Logistics in Dubai",
      "subject2": "Quick question for Ahmed"
    }
  ]
}

CSV Structure
The engine expects a CSV with these headers:
Name, Company, Industry, City

Scalability Note
The current version uses an asynchronous thread pool to process rows. To scale this to 10k+ rows, the architecture is designed to easily integrate Apache Kafka for distributed queuing and horizontal scaling across multiple Docker containers.

Local Setup
1. Clone the repo: git clone https://github.com/Manas-Rastogi/moxsend-engine.git
2. Environment Variables: Set your MONGODB_URI and GROQ_API_KEY in application.yml.
3. Run: mvn spring-boot:run
