# Todo API — CI/CD Pipeline & Kubernetes Deployment

Spring Boot REST API (`GET/POST /todos`, `GET /hello`, `/actuator/health`) pushed through:
**Git → Maven → Jenkins (CI) → Ansible → Docker → Kubernetes (scale/rollback)**

| Enhancement | Where |
|---|---|
| CI: automated tests | `Jenkinsfile` Test stage, `src/test/...` |
| CI: build versioning (build no. + Git SHA) | `Jenkinsfile` |
| CI: GitHub Actions | `.github/workflows/ci.yml` |
| Deploy: multi-stage Dockerfile | `Dockerfile` |
| Deploy: ConfigMap | `k8s/configmap.yaml` |
| Deploy: liveness/readiness probes | `k8s/deployment.yaml` |
| Deploy: health endpoint | Spring Actuator (`/actuator/health`) |
| Deploy: rollback demo | Section C5 below |

> For marks, you only need ONE enhancement per component. Pick one to feature in the report; the others are bonus.

---
## A. Run locally (do this first)
```bash
mvn clean package
java -jar target/todo-api.jar
curl localhost:8080/hello
curl -X POST localhost:8080/todos -H "Content-Type: application/json" -d '{"title":"first task"}'
curl localhost:8080/todos
curl localhost:8080/actuator/health
```

## B. Component 1 — Git + Jenkins
### B1. Git (need ≥2 branches + a merge)
```bash
git init -b main
git add pom.xml src .gitignore README.md && git commit -m "Initial Todo API"
git checkout -b feature/hello-endpoint
# make a small change (e.g. tweak message in application.properties)
git commit -am "Add hello endpoint message"
git checkout -b feature/ci-pipeline
git add Jenkinsfile .github && git commit -m "Add Jenkinsfile and GitHub Actions"
git checkout main
git merge --no-ff feature/hello-endpoint -m "Merge feature/hello-endpoint"
git merge --no-ff feature/ci-pipeline -m "Merge feature/ci-pipeline"
git add . && git commit -m "Add Docker, Ansible, K8s files"
git remote add origin https://github.com/<you>/todo-devops.git
git push -u origin --all
git log --oneline --graph --all        # <-- screenshot this
```
(Better: push branches and merge via a GitHub Pull Request.)

### B2. Jenkins
```bash
docker run -d --name jenkins -p 8081:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts-jdk17
```
Jenkins needs Maven + JDK 17: either *Manage Jenkins → Tools* (auto-install Maven) or exec into the container and `apt-get install -y maven`.
1. New Item → **Pipeline** → *Pipeline script from SCM* → Git → your repo URL, branch `*/main`, Script Path `Jenkinsfile`.
2. Trigger comes from the Jenkinsfile (`pollSCM`); also tick *Poll SCM* in job config for the screenshot.
3. Push a small change → build starts by itself. Screenshot: job config, console output (`Finished: SUCCESS`), archived artifacts (`todo-api.jar` and `todo-api-<n>-<sha>.jar`), test results.

## C. Component 2 — Ansible + Docker + Kubernetes
### C1. Ansible (run from Linux/WSL with systemd)
```bash
mvn clean package -DskipTests          # ansible copies target/todo-api.jar
cd ansible
ansible-playbook -i inventory playbook.yml --ask-become-pass
curl localhost:8080/hello
# run it a 2nd time: "changed=0" shows idempotency; change the JAR -> handler restarts the service
```
Uses: variable (`app_port`), handler (`Restart todo-api`), role (`roles/todo_app`).
If your host has no systemd (plain Docker container), use a VM/WSL2 with systemd enabled instead.

### C2. Docker
```bash
docker build -t todo-api:1.0 .
docker run --rm -p 8080:8080 todo-api:1.0
curl localhost:8080/hello
```

### C3. Kubernetes (minikube)
```bash
minikube start
eval $(minikube docker-env)            # build image INSIDE minikube (PowerShell: minikube docker-env | Invoke-Expression)
docker build -t todo-api:1.0 .
kubectl apply -f k8s/configmap.yaml -f k8s/deployment.yaml -f k8s/service.yaml
kubectl get pods
kubectl get svc
curl $(minikube service todo-api-service --url)/hello      # run a few times: "host" changes = different pods
```

### C4. Scaling
```bash
kubectl get pods
kubectl scale deployment todo-api --replicas=5
kubectl get pods                        # 5 pods
kubectl scale deployment todo-api --replicas=3
kubectl get pods
```

### C5. Rollback demo
```bash
kubectl set image deployment/todo-api todo-api=todo-api:broken    # image doesn't exist -> bad rollout
kubectl rollout status deployment/todo-api --timeout=30s
kubectl get pods                         # new pod: ErrImagePull / ImagePullBackOff; old pods keep serving
kubectl rollout history deployment/todo-api
kubectl rollout undo deployment/todo-api
kubectl get pods && curl $(minikube service todo-api-service --url)/hello
```

### C6. ConfigMap demo
```bash
kubectl edit configmap todo-api-config           # change APP_MESSAGE
kubectl rollout restart deployment/todo-api      # pods pick up new value
curl $(minikube service todo-api-service --url)/hello
```

### What /actuator/health gives Prometheus
Health status UP/DOWN of the app; Kubernetes uses `/actuator/health/liveness` and `/readiness`. For Prometheus you'd add `micrometer-registry-prometheus` and scrape `/actuator/prometheus` (JVM memory, CPU, HTTP request counts).

## D. Submission checklist
- [ ] Report (per component: Abstract, Introduction, Implementation, Enhancement + why, Results, Conclusion, References)
- [ ] This folder as source code · [ ] GitHub link in report and `repo_link.txt` · [ ] course coordinator added as collaborator
- [ ] Screenshots listed in the assignment (git graph, Jenkins config/console/artifact/enhancement, ansible output, docker build, pods before/after scaling, svc, curl response, enhancement)

## E. Resume lines
**Todo API — CI/CD Pipeline & Kubernetes Deployment | github.com/<you>/todo-devops**
Java/Spring Boot, Maven, Git, Jenkins, Docker, Ansible, Kubernetes
- Built a REST API (task management) and automated its build pipeline using Jenkins, triggered on every Git push.
- Added automated JUnit tests and commit-SHA versioned artifacts to the CI pipeline.
- Containerized with a multi-stage Dockerfile and deployed on Kubernetes with 3 replicas, using a Service for routing.
- Configured server setup using an Ansible playbook with roles and handlers.
- Added ConfigMap-based configuration, health probes, and demonstrated scaling and rollback.
# trigger
# trigger
