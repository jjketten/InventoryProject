import { Platform } from 'react-native';
import * as Notifications from 'expo-notifications';
import { Reminder as ReminderDTO } from '../../types/reminder'; // Adjust path if needed
import { APIURL } from '../config';
import { SchedulableTriggerInputTypes } from 'expo-notifications';

let pollingInterval: ReturnType<typeof setInterval> | null = null;

export async function initializeReminderScheduler() {
  console.log('[ReminderScheduler] Initializing...');

  if (Platform.OS === 'web') {
    requestWebNotificationPermission();
  } else {
    await Notifications.requestPermissionsAsync();
  }

  await fetchAndScheduleReminders();

  //fetch every 5 minutes
  pollingInterval = setInterval(fetchAndScheduleReminders, 5 * 60 * 1000);
}

async function fetchAndScheduleReminders() {
  try {
    console.log('[ReminderScheduler] Fetching reminders...');
    const res = await fetch(`${APIURL}/reminders`, {
      headers: { 'X-Tenant-ID': 'test_schema2' }
    });
    const reminders: ReminderDTO[] = await res.json();

    const now = new Date();
    const upcoming = reminders.filter(r => {
      const reminderTime = new Date(r.dateTime);
      const diffHours = (reminderTime.getTime() - now.getTime()) / (1000 * 60 * 60);
      return diffHours >= 0 && diffHours <= 24;
    });

    console.log(`[ReminderScheduler] Scheduling ${upcoming.length} upcoming reminders`);

    for (const reminder of upcoming) {
      const reminderDate = new Date(reminder.dateTime);
      if (Platform.OS === 'web') {
        scheduleWebNotification(reminder.description + (reminder.itemName ? reminder.itemName : ""), reminderDate);
      } else {
        await scheduleMobileNotification(reminder.description + (reminder.itemName ? reminder.itemName : ""), reminderDate);
      }
    }
  } catch (err) {
    console.error('[ReminderScheduler] Error fetching reminders:', err);
  }
}

function requestWebNotificationPermission() {
  if (Notification.permission !== 'granted') {
    Notification.requestPermission();
  }
}

function scheduleWebNotification(itemName: string,dateTime: Date) {
  const now = new Date();
  const delay =dateTime.getTime() - now.getTime();

  if (delay > 0) {
    setTimeout(() => {
      if (Notification.permission === 'granted') {
        new Notification('Reminder!', {
          body: `Don't forget: ${itemName}`,
        });
      }
    }, delay);
  }
}

async function scheduleMobileNotification(itemName: string,dateTime: Date) {
  await Notifications.scheduleNotificationAsync({
    content: {
      title: 'Reminder!',
      body: `Don't forget: ${itemName}`,
      sound: true,
    },
    trigger: {
        type: SchedulableTriggerInputTypes.CALENDAR,
        year:dateTime.getFullYear(),
        month:dateTime.getMonth() + 1, // Months are 0-indexed
        day:dateTime.getDate(),
        hour:dateTime.getHours(),
        minute:dateTime.getMinutes(),
        second:dateTime.getSeconds(),
        repeats: false,
    },
  });
}


export function stopReminderScheduler() {
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
}
