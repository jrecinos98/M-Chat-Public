# M-Chat-Android
Messaging application for Android.

__ProtoBuffer Schema__:
=======================
  Messages:
  ---------
  - ### Type 0 **(AUTH)**: 
      1. **client** -> **server:** Client indicates it wants to authenticate.
          - **UID:** The unique identifier of the client attempting to authenticate
          - **Token:** Token used for secure authentication.
      2. **server** -> **client:** Server ACK signals that authentication was successful.
          - Server sends back an ACK message of type 0
  - ### Type 1 **(MESSAGE)**:
      1. **client** -> **server:** Client indicates that it is sending a message (payload field not empty). 
          - **Message ID:** The **client** assigns the message a unique id that the **server** utilizes when sending an ACK
          - ### Payload:
              - **OP Code:** The kind of message the client is sending (Text, Image, Audio,Video, PDF, etc...)
              - **Time Stamp:** The time at which the message was created.
              - **Destination ID:** The ID of the destination user.
              - **Key:** The cryptographic key used to encrypt the message
              - **Text (OPTIONAL):** If the op code is for a text message this field is used.
              - **Data (OPTIONAL):** If the op code involves any kind of message that is not text then this field will be used.
              - **Format (OPTIONAL):** If the data field it is set this field will indicate what type of format the data is encoded in.
  - ### Type 2 **(ACK)**:
      1. **server** -> **client:** Server ACK for a received message.
          - **Message ID:** ID of the received message the server is sending an ACK for.
      2. **client** -> **server:** Client ACK for a received message
          - **Message ID:** same as above
  - ### Type 3:
      1. **Reserved (Potentially for Single person message)**
  - ### Type 5:
      1. **Reserved (Potentially for group messages)**
  - ### Type 6:
      1. **Can represent a commonly used command the client requests from the server**
