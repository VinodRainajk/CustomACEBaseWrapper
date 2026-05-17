package com.qa.framework.payload;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayloadRegistryTest {

    @AfterEach
    void tearDown() {
        PayloadRegistry.clear();
    }

    @Test
    void loadsApiOperationFromUsersApiPayload() {
        PayloadRegistry.setActiveFeature("users-api");
        PayloadOperation op = PayloadRegistry.getOperation("create_post");
        assertEquals(PayloadOperationType.API, op.getType());
        assertEquals("requests/users-api/create-post.json", op.getFile());
    }

    @Test
    void loadsSqlOperationFromUsersApiPayload() {
        PayloadRegistry.setActiveFeature("users-api");
        PayloadOperation op = PayloadRegistry.getOperation("count_users_by_email");
        assertEquals(PayloadOperationType.SQL, op.getType());
        assertEquals("sql/users-api/count-users.sql", op.getFile());
    }
}
