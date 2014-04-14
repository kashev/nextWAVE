using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Net;
using System.Collections.Specialized;
using System.Diagnostics.Tracing;

namespace SparkCoreTest
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            
            InitializeComponent();
            ReturnLabel.Content = "Start";
            string url = string.Empty;
           //var source = new EventSource()
           // source.onmessage = function (e) {
            //   console.log(e.data);
//}
        }

        private void Button_Click_1(object sender, RoutedEventArgs e)
        {
            try
            {
                using (var client = new WebClient())
                {
                    var values = new NameValueCollection();
                    string timevalue = "1000";
                    timevalue = TimeValueBox.Text;
                    values["access_token"] = "4348526a1c0932c678d6e971ce456b9d2ea4a1f5";
                    values["time"] = timevalue;

                    //var response = 
                    var response =  client.UploadValues("https://api.spark.io/v1/devices/48ff70065067555028111587/cook", values);

                        var responseString = Encoding.Default.GetString(response);
                    ReturnLabel.Content = responseString;
                }
            }
            catch (Exception ex) {
                ReturnLabel.Content = ex.ToString();
            }
            
        }
        private void Button_Click_2(object sender, RoutedEventArgs e)
        {
            try
            {
                using ( var client = new WebClient())
                {
                    var values = new NameValueCollection();

                    values["access_token"] = "4348526a1c0932c678d6e971ce456b9d2ea4a1f5";

                    var response = client.UploadValues("https://api.spark.io/v1/devices/48ff70065067555028111587/stopcook", values);

                    var responseString =  Encoding.Default.GetString(response);
                    ReturnLabel.Content = responseString;
                }
            }
            catch (Exception ex)
            {
                ReturnLabel.Content = ex.ToString();
            }

        }

        private void Button_Click_3(object sender, RoutedEventArgs e)
        {
            try
            {
                using (var client = new WebClient())
                {
                    var values = new NameValueCollection();

                    values["access_token"] = "4348526a1c0932c678d6e971ce456b9d2ea4a1f5";

                    var response = client.UploadValues("https://api.spark.io/v1/devices/48ff70065067555028111587/opendoor", values);

                    var responseString = Encoding.Default.GetString(response);
                    ReturnLabel.Content = responseString;
                }
            }
            catch (Exception ex)
            {
                ReturnLabel.Content = ex.ToString();
            }

        }
    }
}
