// fastlearner-sdk.js
const QualityCenter = (() => {
    const BASE_URL = 'https://qualitycenter.fastlearner.ai/'; // Quality Center base URL
    // const BASE_URL = 'http://localhost:4200'; // Quality Center base URL

  
    const openLoginPopup = (clientId, callback) => {
      const popupWidth = 500;
      const popupHeight = 600;
      const popupLeft = (window.innerWidth - popupWidth) / 2;
      const popupTop = (window.innerHeight - popupHeight) / 2;
  
      const loginUrl = `${BASE_URL}/fl-login?clientId=${clientId}`;
      const loginPopup = window.open(
        loginUrl,
        'Quality Center Login',
        `width=${popupWidth},height=${popupHeight},top=${popupTop},left=${popupLeft}`
      );
  
      // Listen for the token from Quality Center
      const messageListener = (event) => {
        if (event.origin === BASE_URL) {
          const token = event.data.token;
  
          if (token) {
            // Invoke the callback with the token
            callback(token);
  
            // Clean up: Close the popup and remove event listener
            if (loginPopup && !loginPopup.closed) {
              loginPopup.close();
            }
            window.removeEventListener('message', messageListener);
          }
        }
      };
  
      // Add event listener
      window.addEventListener('message', messageListener);
    };
  
    return {
      signIn: (clientId, callback) => {
        if (!clientId) {
          throw new Error('clientId is required');
        }
        if (typeof callback !== 'function') {
          throw new Error('Callback function is required');
        }
        openLoginPopup(clientId, callback);
      },
    };
  })();
  
  // Export as a global object
  window.Quality.Center = QualityCenter;
  