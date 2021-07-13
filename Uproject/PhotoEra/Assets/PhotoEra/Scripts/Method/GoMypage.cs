﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GoMypage : MonoBehaviour
{
    private AndroidJavaObject currentActivity;
    private int sysNumber = 1;

    public void OnClick() {
        AndroidJavaClass ajc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        currentActivity = ajc.GetStatic<AndroidJavaObject>("currentActivity");

        if(currentActivity != null)
         {
             sysNumber = currentActivity.Call<int>("GoMypage");
         }
        if(sysNumber == 0){
        Application.Quit();
        }
       Debug.Log("Button Click");
   }

}
