<template>
  <v-fade-transition>
    <v-card
      v-if="modelValue"
      class="chat-window d-flex flex-column"
      elevation="12"
      width="380"
      height="50vh"
    >
      <v-toolbar color="primary" dark dense>
        <v-toolbar-title>AI Bot</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeChat">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-toolbar>
      <v-card-text class="flex-grow-1 overflow-y-auto">
        <div class="pa-4">
          欢迎使用ai bot
        </div>
      </v-card-text>
      <v-card-actions>
        <v-text-field
          v-model="message"
          label="输入消息..."
          variant="outlined"
          dense
          hide-details
          append-inner-icon="mdi-send"
          @click:append-inner="sendMessage"
          @keydown.enter="sendMessage"
        ></v-text-field>
      </v-card-actions>
    </v-card>
  </v-fade-transition>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  modelValue: Boolean
});

const message = ref('');

const emit = defineEmits(['update:modelValue']);

const closeChat = () => {
  emit('update:modelValue', false);
};

const sendMessage = () => {
  if (message.value.trim()) {
    console.log('Sending message:', message.value);
    message.value = '';
  }
};
</script>

<style scoped>
.chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}
</style>
