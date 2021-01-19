<?php

// Your Mysql configuration
$mysqlHost = "shareddb-p.hosting.stackcp.net";
$mysqlUser = "pushpanel-313135d3a9";
$mysqlPwd = "rohitddd1";
$mysqlDbname = "pushpanel-313135d3a9";



class GCM {
    function __construct(){}
    
    public function send_notification($registatoin_ids,$data) {
        
        $google_api_key = "AAAAbZnUWaQ:APA91bGrQhnjpPAdJCqu8ULOETYFNrjtaE-HO4JCBaD8Sag97vKU56u1x0bsnA0gASPt2stH9guHJEH2_zTCwTH6LOvc6PzTfli67Fysk7biYa9RCqVUC6I9lX_HErcvbG5Sj8cGQCBJ"; // <-- Insert your Google API Key

        $url = 'https://fcm.googleapis.com/fcm/send';
        $fields = array(
            'registration_ids' => $registatoin_ids,
            'notification' => array('title' => $data['title'],
                'body' => $data['description'],
                'click_action' => "OPEN_MAIN_1",
                'icon' => 'ic_launcher'),
            'data' => array('link' => $data['link'])
        );

        $headers = array(
            'Authorization:key =' . $google_api_key,
            'Content-Type: application/json'
        );
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
        $result = curl_exec($ch);
        if($result===FALSE){
            die("Curl failed: ".curl_error($ch));
        }
        curl_close($ch);
    }
}



// Create connection
$conn = mysqli_connect($mysqlHost, $mysqlUser, $mysqlPwd, $mysqlDbname);

// Check connection
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}


$result = $conn->query("SELECT * FROM users WHERE user_android_token IS NOT NULL AND user_android_token <> ''");

$android_tokens = array();
$x=0;
$i=0;
if ($result->num_rows > 0) {
    // output data of each row
    while($row = $result->fetch_assoc()) {

  $android_tokens[$i][] = $row["user_android_token"];
  $x++;
  // I need divide the array for 1000 push limit send in one time
  if ($x % 800 == 0) {
    $i++;
  }     
    }
} else {
    echo "0 results";
}
$ip= $_SERVER['REMOTE_ADDR'];
$result_check = $conn->query("SELECT * FROM `notifications` WHERE notification_sender_ip = '$ip' && notification_date > DATE_SUB(NOW(),INTERVAL 5 MINUTE)");
if ($result_check->num_rows > 2) {
        die('Anti flood protection. You can send only 3 notifications every 5 minutes!. This is just a demo push panel, buy this from codecanyon and install into your hosting. Thanks!');
}

$title = $_POST['title'];
$msg = $_POST['message'];
$link = $_POST['link'];

if ($android_tokens != array()) {
    $gcm=new GCM();
    $data=array("title"=>$title,"description"=>$msg,"link"=>$link);
    foreach ($android_tokens as $tokens) {
      $result_android = $gcm->send_notification($tokens,$data);
      sleep(1);
    }
    
    $sql = "INSERT INTO notifications (notification_title, notification_text, notification_extra, notification_sender_ip) VALUES ('$title', '$msg', '$link', '{$_SERVER['REMOTE_ADDR']}')";
    mysqli_query($conn, $sql);
}

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>Android Push Panel</title>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
     <div class="container">
      <div class="header clearfix">
       
        <h3 class="text-muted">Android Push Panel</h3>
      </div>

      <div class="jumbotron">
        <h1>Good!</h1>
       <p>You have sent <?php echo $x;?> push notification.</p>
</form>
      </div>

      <div class="row marketing">
        <div class="col-lg-6">


          <h4>Requirements</h4>
          <p>WebApp Essentials v1+ & PHP5 & Mysql database</p>

          <h4>Where can I download WebApp Essentials?</h4>
          <p><a href="http://codecanyon.net/item/native-web-app-essentials/13183329">Click here to download from CodeCanyon</a></p>
        
          
        </div>

        <div class="col-lg-6">
          <h4>How to works?</h4>
             <p>When the app WebApp Essentials starts, it will send its token to the server configured in it. The token is saved in the database, and this script allows you to cycle all the tokens and send a push notification.</p>

          

        </div>
      </div>

      <footer class="footer">
          <p>&copy; <a href="http://www.digitalborder.net">DigitalBorder</a> 2015-2016</p>
      </footer>

    </div> 

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
  </body>
</html>