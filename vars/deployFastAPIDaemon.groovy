def call(String appDirectory) {
    echo "Starting deployment..."
    
    sh '''
    set -e 
    
    # Debug: Print the variable to the Jenkins console
    APP_DIR="''' + appDirectory + '''"
    echo "Attempting to change directory to: $APP_DIR"
    
    # Check if the directory exists first
    if [ ! -d "$APP_DIR" ]; then
        echo "Error: Directory $APP_DIR does not exist!"
        exit 1
    fi
    
    sudo /usr/bin/mkdir -p $APP_DIR
    sudo /usr/bin/rsync -av --exclude='.git' $WORKSPACE/ $APP_DIR/
    sudo /usr/bin/chown -R huz:www-data $APP_DIR
    
    # Now try the cd
    cd "$APP_DIR"
    
    # ... rest of your script
    '''
}
