rootProject.name = "DataComponentAPI"

include(
    "api",
    "dist",
    "test-plugin:library",
    "test-plugin:shade",

    "nms:v1_20_R4",
    "nms:v1_21_R1"
)