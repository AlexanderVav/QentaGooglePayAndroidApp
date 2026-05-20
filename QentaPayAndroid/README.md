# QPay -> GooglePay Tester App 📱💳

With this app, we wanted to showcase how to use the GooglePay implementation via a WebView in your native Android app.

**Context:** In the middle of 2025, Google changed its accessibility policies for GooglePay usage in custom apps. Before this update, it was not possible to reliably use GooglePay within a WebView. With Google changing its access policy, it is now possible to easily implement GooglePay via QPay directly into your custom app.

This project serves as a reference implementation for that flow, including handling payment states via JavaScript bridges and providing a seamless native UX.

## ✨ Key Features

* **WebView Payment Request Support:**

    * Explicitly enables `WebSettingsCompat.setPaymentRequestEnabled` to allow Google Pay overlays on top of the WebView.

    * **Immersive Fullscreen Experience:** * Uses `FLAG_LAYOUT_NO_LIMITS` to render the WebView behind the status bar and navigation bar.
    * Removes the Action Bar for a modern, edge-to-edge look.
  
* **Smart WebView Bridge (`AndroidBridge`):**

    * **Aggressive Event Listening:** Injects JavaScript to listen for global `click` and `touchend` events using `useCapture=true`. This ensures interactions are caught even if the website stops event propagation.
    * **Hybrid State Detection:** Detects payment success/failure using a 3-step fallback mechanism:
        1.  **URL Parameters:** Checks for `?paymentState=SUCCESS` in the current URL.
        2.  **JSON Parsing:** Parses the visible text or HTML body if the response is raw JSON.
        3.  **Regex/Text Search:** Scans the `innerText` using Regex to find patterns like `paymentState: "SUCCESS"` in unstructured HTML.
      
* **Native Success Screen:** * Transitions from the WebView to a native Android Activity.
    * Features a celebration animation using the **Konfetti** library.
  
* **Robust Navigation Handling:** * Clears the back stack (`FLAG_ACTIVITY_CLEAR_TASK`) upon success to prevent accidental double-payments.
    * Handles the hardware "Back" button within the WebView history before closing the activity.

## 🛠 Tech Stack

* **Language:** Java
* **Minimum SDK:** Android 24 (Nougat)
* **Architecture:** Multi-Activity (Main -> Payment -> Success)
* **Components:** Android WebView, AndroidX, ConstraintLayout
* **External Libraries:** [Konfetti](https://github.com/DanielMartinus/Konfetti) (for particle animations)

## ⚙️ Settings

* **Docker**: Please git clone the repo for QPay and set it up with [Docker](https://github.com/hobex/qcp-example-php.git). Important settings in the `/includes/config.inc.php` file are:
* `param = $api['endpoint'] = getenv('QCP_ENDPOINT') ?: 'https://papi.hobex.at/page/init.php';` to change to your url endpoint. If you change this endpoint please be advised to either use `paymentState` on your success page or implement your keyword in the app.
* `param = $paymentTypes['GOOGLEPAY'] = 'GOOGLEPAY';` is the paymentType that you want to use. (Credit Card, PayPal, GooglePay, Apple Pay).
* Please be advised that without the page returns an error (404 page not found).
* Depending on if you either want to use Chrome Custom Tabs or Webviews you would need to set the variable "isUsingWebView".

## 🛜 NGROK

If you would like to test your application on a real device, you will need to use ngrok. It allows you to create a tunnel and make your locally hosted application available through a randomly generated link.

Please copy this link into the `NGROK_URL` variable. Be advised that your company’s proxy settings may interfere, causing ngrok to not work properly.

Link to ngrok documentation: [NGROK](https://ngrok.com/docs/what-is-ngrok)
