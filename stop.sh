#!/bin/bash

# Lambrk Backend Stop Script
# This script stops all running Lambrk services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Lambrk Backend Stop Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Function to find and kill process by port
kill_process_on_port() {
    local port=$1
    local service_name=$2
    
    # Find process using the port
    local pid=$(lsof -ti:$port 2>/dev/null || echo "")
    
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}Stopping $service_name on port $port (PID: $pid)...${NC}"
        kill -TERM $pid 2>/dev/null || kill -KILL $pid 2>/dev/null
        
        # Wait for process to terminate
        local count=0
        while kill -0 $pid 2>/dev/null && [ $count -lt 10 ]; do
            sleep 0.5
            count=$((count + 1))
        done
        
        if kill -0 $pid 2>/dev/null; then
            echo -e "${RED}✗ Failed to stop $service_name${NC}"
            return 1
        else
            echo -e "${GREEN}✓ $service_name stopped${NC}"
            return 0
        fi
    else
        echo -e "${YELLOW}$service_name is not running on port $port${NC}"
        return 0
    fi
}

# Function to kill processes by name pattern
kill_processes_by_name() {
    local pattern=$1
    local service_name=$2
    
    # Find processes matching the pattern
    local pids=$(pgrep -f "$pattern" 2>/dev/null || echo "")
    
    if [ -n "$pids" ]; then
        echo -e "${YELLOW}Stopping $service_name processes...${NC}"
        for pid in $pids; do
            echo -e "${YELLOW}  Killing process $pid...${NC}"
            kill -TERM $pid 2>/dev/null || kill -KILL $pid 2>/dev/null
        done
        
        # Wait a bit for processes to terminate
        sleep 1
        
        # Check if any are still running
        local remaining=$(pgrep -f "$pattern" 2>/dev/null || echo "")
        if [ -n "$remaining" ]; then
            echo -e "${YELLOW}Force killing remaining processes...${NC}"
            for pid in $remaining; do
                kill -KILL $pid 2>/dev/null || true
            done
        fi
        
        echo -e "${GREEN}✓ $service_name stopped${NC}"
        return 0
    else
        echo -e "${YELLOW}$service_name is not running${NC}"
        return 0
    fi
}

# Function to stop all services
stop_all_services() {
    echo -e "${YELLOW}Stopping all Lambrk services...${NC}"
    echo ""
    
    # Stop services by port (more reliable)
    kill_process_on_port 3100 "API Gateway"
    kill_process_on_port 3101 "Auth Service"
    kill_process_on_port 3102 "Video Service"
    
    echo ""
    
    # Also try to kill by process name patterns (backup method)
    echo -e "${YELLOW}Checking for any remaining processes...${NC}"
    kill_processes_by_name "ts-node-dev.*auth-service" "Auth Service (ts-node-dev)"
    kill_processes_by_name "ts-node-dev.*video-service" "Video Service (ts-node-dev)"
    kill_processes_by_name "ts-node-dev.*gateway" "Gateway (ts-node-dev)"
    kill_processes_by_name "concurrently.*dev:all" "Concurrently"
    
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}All services stopped${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# Function to stop Docker containers (if using Docker)
stop_docker_containers() {
    if command -v docker &> /dev/null; then
        echo -e "${YELLOW}Checking for Docker containers...${NC}"
        
        local containers=$(docker ps --filter "name=lambrk" --format "{{.Names}}" 2>/dev/null || echo "")
        
        if [ -n "$containers" ]; then
            echo -e "${YELLOW}Stopping Docker containers...${NC}"
            docker stop $(docker ps -q --filter "name=lambrk") 2>/dev/null || true
            echo -e "${GREEN}✓ Docker containers stopped${NC}"
        else
            echo -e "${YELLOW}No Docker containers running${NC}"
        fi
    fi
}

# Main execution
main() {
    # Stop Docker containers first (if any)
    stop_docker_containers
    
    echo ""
    
    # Stop all Node.js services
    stop_all_services
    
    echo ""
    echo -e "${GREEN}Shutdown complete!${NC}"
}

# Run main function
main

