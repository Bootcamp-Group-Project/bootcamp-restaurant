#!/bin/bash
TOKEN="EAAOOL3dJRBgBRoBUGAQvoJldPCpU6txa6oBq51kwhQBtq2Cri6FuG9xztZB2j5v8fyZABSGxz9t7OCAs3ZBBTMVZBmKzZAw3ZB5bbIv71WsoOVPWImNJsFI302c2OZA9gBpPZBUP2K7ya3AhUoQH0y8Uo4Rn6oafi38kfqIUvU3cWazSWhEzYm29pVfZAww9e234sQIDGKRq4kEjNEaCyJW3iGLojI353SZCifGAZA9c3v3"

curl -X POST "https://graph.facebook.com/v19.0/1206541779201305/subscribed_apps" \
  -d "subscribed_fields=messages,messaging_postbacks,messaging_referrals" \
  -d "access_token=${TOKEN}"

