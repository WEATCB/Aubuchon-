﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;
using System.Web.Routing;

namespace AubuchonAPI
{
    public class WebApiApplication : System.Web.HttpApplication
    {
        protected void Application_Start()
        {
            GlobalConfiguration.Configure(WebApiConfig.Register);
        }

       //public void Application_BeginRequest(object sender, EventArgs e)
       // {
       //     var context = HttpContext.Current;
       //     var response = context.Response;

       //     // enable CORS
       //     response.AddHeader("Access-Control-Allow-Origin", "*");

       //     if (context.Request.HttpMethod == "OPTIONS")
       //     {
       //         response.AddHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
       //         response.AddHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
       //         response.End();
       //     }
       // }
    }
}
