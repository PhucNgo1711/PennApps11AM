package com.houndify.sample;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.core.model.sdk.ClientMatch;
import com.hound.core.model.sdk.HoundRequestInfo;

import java.util.ArrayList;

/**
 * We use a singleton in order to not hold a memory reference to the host activity since this is registered in the Houndify
 * singleton.
 */
public class StatefulRequestInfoFactory extends DefaultRequestInfoFactory {

    public static StatefulRequestInfoFactory instance;

    private JsonNode conversationState;

    public static StatefulRequestInfoFactory get(final Context context) {
        if (instance == null) {
            instance = new StatefulRequestInfoFactory(context);
        }
        return instance;
    }

    private StatefulRequestInfoFactory(Context context) {
        super(context);
    }

    public void setConversationState(JsonNode conversationState) {
        this.conversationState = conversationState;
    }

    @Override
    public HoundRequestInfo create() {
        final HoundRequestInfo requestInfo = super.create();
        requestInfo.setConversationState(conversationState);

        /*
         * "Client Match"
         *
         * Below is sample code to demonstrate how to use the "Client Match" Houndify feature which
         * lets client apps specify their own custom phrases to match.  To try out this
         * feature you must:
         *
         * 1. Enable the "Client Match" domain from the Houndify website: www.houndify.com.
         * 2. Uncomment the code below.
         * 3. And finally, to see how the response is handled in go to the MainActivity and see
         *    "Client Match" demo code inside of onResponse()
         *
         * This example allows the user to say "turn on the lights", "turn off the lights", and
         * other variations on these phases.
         */

        ArrayList<ClientMatch> clientMatchList = new ArrayList<>();

        // client match 1
        ClientMatch clientMatch1 = new ClientMatch();
        clientMatch1.setExpression("(\"i'm\"|\"everything's\"|\"things're\").(\"safe\"|\"okay\"|\"alright\"|\"fine\")");
//        clientMatch1.setExpression("\"i'm okay\"");

        clientMatch1.setSpokenResponse("Ok, I'll tell your friends and family.");
        clientMatch1.setSpokenResponseLong("Ok, I'll send text messages to your friends and family.");
        clientMatch1.setWrittenResponse("Ok, I'll tell your friends and family.");
        clientMatch1.setWrittenResponseLong("Ok, I'll send text messages to your friends and family.");

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode result1Node = nodeFactory.objectNode();
        result1Node.put("Intent", "Okay");
        clientMatch1.setResult(result1Node);

        // add first client match data to the array/list
        clientMatchList.add(clientMatch1);

        // client match 2
        ClientMatch clientMatch2 = new ClientMatch();
        clientMatch2.setExpression("(\"i need help\"|\"help me\").[\"please\"]");
//        clientMatch2.setExpression("\"i need help\"");
        clientMatch2.setSpokenResponse("Ok, I'll find help.");
        clientMatch2.setSpokenResponseLong("Ok, I'll tell your friends and family to get help.");
        clientMatch2.setWrittenResponse("Ok, I'll find help.");
        clientMatch2.setWrittenResponseLong("Ok, I'll tell your friends and family to get help.");

        ObjectNode result2Node = nodeFactory.objectNode();
        result2Node.put("Intent", "Help");
        clientMatch2.setResult(result2Node);

        // add next client match data to the array/list
        clientMatchList.add(clientMatch2);

        // add as many more client match entries as you like...


        // add the list of matches to the request info object
        requestInfo.setClientMatches(clientMatchList);

        return requestInfo;
    }
}
