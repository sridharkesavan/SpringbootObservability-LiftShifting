
# SpecialistSearch

SpecialistSearch is a Spring Boot–based microservice that exposes APIs for searching and managing “specialists” (for example, professionals, experts, or service providers). 
It is designed as a playground for production-like practices: containerization, local Kubernetes (kind), and monitoring with Prometheus and Grafana.

## Features

- Spring Boot REST APIs for specialist search and management.  
- Production-style configuration with profiles and externalized settings.  
- Metrics exposed via Spring Boot Actuator and Prometheus endpoint.  
- Ready to be run locally on Docker Desktop with kind, Prometheus, and Grafana.
- Step by step guide:
                   https://medium.com/@sridharke7/how-i-jam-with-spring-boot-on-kubernetes-locally-a-kind-prometheus-and-grafana-setup-guide-935922e3133d?postPublishedType=repub

## Prerequisites

- Java 17+  
- Maven 3+  
- Git  
- Docker Desktop  
- kind (Kubernetes in Docker)  
- kubectl  
- Optional: Prometheus and Grafana Docker images (pulled automatically in most setups)

## How to clone and run locally (plain Spring Boot)

1. Clone the repository:
   - git clone https://github.com/your-username/SpecialistSearch.git  
   - cd SpecialistSearch

2. Build the project:
   - mvn clean package

3. Run the application:
   - mvn spring-boot:run  
   The app will start on http://localhost:8080 (or whatever port you’ve configured).

4. Basic health check:
   - Open http://localhost:8080/actuator/health in your browser or use curl.

## Running with Docker

1. Build the Docker image:
   - docker build -t specialist-search:local .

2. Run the container:
   - docker run -p 8080:8080 --name specialist-search specialist-search:local

3. Access the service:
   - http://localhost:8080

## Running on kind + Prometheus + Grafana

Note: This assumes you’re following the “production-like local” setup described in your checklist document.

1. Create a kind cluster:
   - kind create cluster --name specialist-search

2. Load the Docker image into kind:
   - kind load docker-image specialist-search:local --name specialist-search

3. Apply Kubernetes manifests:
   - kubectl apply -f k8s/deployment.yaml  
   - kubectl apply -f k8s/service.yaml  
   - kubectl apply -f k8s/ingress.yaml   (if you’re using ingress)

4. Deploy Prometheus and Grafana:
   - kubectl apply -f monitoring/prometheus.yaml  
   - kubectl apply -f monitoring/grafana.yaml

5. Port-forward to access dashboards:
   - Prometheus:  
     - kubectl port-forward svc/prometheus 9090:9090  
     - Open http://localhost:9090
   - Grafana:  
     - kubectl port-forward svc/grafana 3000:3000  
     - Open http://localhost:3000

6. Metrics endpoint (from Spring Boot):
   - http://<service-host>/actuator/prometheus

## Configuration

- Application properties:
  - src/main/resources/application.properties (or application.yml)  
- Environment-specific overrides via:
  - application-dev.properties  
  - application-prod.properties  

You can customize ports, database connections, and actuator exposure via these files or environment variables.

## Next steps

The goal of SpecialistSearch is not just to run locally, but to be easily “left-shifted” to Azure AKS with minimal configuration changes. 
Once everything runs smoothly on kind, you can adapt the same manifests and monitoring setup for your Azure environment and plug into cloud-native observability.
