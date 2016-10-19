# Deployment as a Service (DaaS)

Deployment as a Service on Hybrid Cloud is a solution where the developer can leverage various cloud platforms for deployment of the application without knowing the deployment process. The platform provides option for the enterprise to deploy or migrate to a public cloud. We are using Kubernetes as backend service to deploy Docker containers. The platform is capable of reading the existing Kubernetes configuration from the currently deployed environment and could replicate in a different destination cloud. Also, the platform provides capabilities to accept Docker configuration file and deploy through Kubernetes.  The platform is scalable as it creates separate Kubernetes instance on for each project and we call it management server for that cluster.

# Kubernetes Commands

sudo keytool -import -trustcacerts -keystore cacerts -alias kubernetes1 -file ~/Documents/295B/kubernetes-master.crt

export KUBERNETES_PROVIDER=aws;
export MASTER_SIZE=t2.micro;
export NODE_SIZE=t2.micro;
export NUM_NODES=3
