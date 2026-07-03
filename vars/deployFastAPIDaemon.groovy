def call(String appDirectory) {
    echo "Starting deployment..."
    
    sh '''
    set -e 
    
    APP_DIR="''' + appDirectory + '''"
    
    echo "Deploying to $APP_DIR..."
    sudo /usr/bin/mkdir -p $APP_DIR
    sudo /usr/bin/rsync -av --exclude='.git' $WORKSPACE/ $APP_DIR/
    sudo /usr/bin/chown -R huz:www-data $APP_DIR
    
    cd $APP_DIR
    
    if [ ! -d "venv" ]; then
        echo "Creating Python virtual environment..."
        python3 -m venv venv
    fi
    
    source venv/bin/activate
    pip install --upgrade pip
    pip install -r requirements.txt
    
    echo "Restarting FastAPI service..."
    sudo /usr/bin/systemctl daemon-reload
    sudo /usr/bin/systemctl restart fastapi
    
    echo "Deployment successful!"
    '''
}
