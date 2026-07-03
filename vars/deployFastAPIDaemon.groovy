def call(String appDirectory) {
    echo "Starting deployment..."
    
    // Change to triple single quotes
    sh '''
    set -e 
    
    # Now Groovy ignores the $ sign and passes it directly to Bash
    APP_DIR="''' + appDirectory + '''"
    
    echo "Deploying to $APP_DIR..."
    sudo mkdir -p $APP_DIR
    sudo rsync -av --exclude='.git' \$WORKSPACE/ \$APP_DIR/
    sudo chown -R huz:www-data $APP_DIR
    cd \$APP_DIR
    
    if [ ! -d "venv" ]; then
        echo "Creating Python virtual environment..."
        python3 -m venv venv
    fi
    
    source venv/bin/activate
    pip install --upgrade pip
    pip install -r requirements.txt
    
    echo "Restarting FastAPI service..."
    sudo systemctl daemon-reload
    sudo systemctl restart fastapi
    
    echo "Deployment successful!"
    '''
}
