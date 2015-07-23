  // A simple error handler to be used throughout this demo.
  function errorHandler(error) {
    var message = '';

    switch (error.code) {
      case FileError.SECURITY_ERR:
        message = 'Security Error';
        break;
      case FileError.NOT_FOUND_ERR:
        message = 'Not Found Error';
        break;
      case FileError.QUOTA_EXCEEDED_ERR:
        message = 'Quota Exceeded Error';
        break;
      case FileError.INVALID_MODIFICATION_ERR:
        message = 'Invalid Modification Error';
        break;
      case FileError.INVALID_STATE_ERR:
        message = 'Invalid State Error';
        break;
      default:
        message = 'Unknown Error';
        break;
    }

    console.log(message);
  }

    // Request a FileSystem and set the filesystem variable.
  function initFileSystem() {
    navigator.webkitPersistentStorage.requestQuota(1024 * 1024,
      function(grantedSize) {

        // Request a file system with the new size.
        window.requestFileSystem(window.PERSISTENT, grantedSize, function(fs) {

          // Set the filesystem variable.
          filesystem = fs;

          // Setup event listeners on the form.
          setupFormEventListener();

          // Update the file browser.
          listFiles();

        }, errorHandler);

      }, errorHandler);
  }
