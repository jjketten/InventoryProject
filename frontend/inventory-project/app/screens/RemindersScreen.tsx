import React, { useEffect, useState } from 'react';
import { View, ScrollView, Switch } from 'react-native';
import { Button, useTheme } from 'react-native-paper';
import { Reminder } from '../../types/reminder';
import GenericTable from '../../components/GenericTable';
import { ColumnConfig } from '../../components/ColumnConfig';

const ReminderScreen: React.FC = () => {
  const { colors } = useTheme();
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [showCompleted, setShowCompleted] = useState(false);

  const headers = {
    'X-Tenant-ID': 'test_schema2',
    'Content-Type': 'application/json',
  };

  const fetchReminders = async () => {
    try {
      const response = await fetch('http://127.0.0.1:9000/api/reminders', {
        method: 'GET',
        headers,
      });
      const data: Reminder[] = await response.json();
      setReminders(data);
    } catch (error) {
      console.error('Failed to fetch reminders:', error);
    }
  };

  useEffect(() => {
    fetchReminders();
  }, []);

  const handleComplete = async (index: number) => {
    const reminder = reminders[index];
    try {
      await fetch(
        `http://127.0.0.1:9000/api/reminders/${reminder.itemID}/${reminder.purchaseID}/complete`,
        { method: 'POST', headers }
      );
      fetchReminders();
    } catch (e) {
      console.error('Failed to mark reminder complete:', e);
    }
  };

  const handleDelete = async (index: number) => {
    const reminder = reminders[index];
    try {
      await fetch(
        `http://127.0.0.1:9000/api/reminders/${reminder.itemID}/${reminder.purchaseID}`,
        { method: 'DELETE', headers }
      );
      fetchReminders();
    } catch (e) {
      console.error('Failed to delete reminder:', e);
    }
  };

  const columns: ColumnConfig<Reminder>[] = [
    { key: 'date', label: 'Purchase Date' },
    { key: 'purchaseID', label: 'Purchase ID' },
    { key: 'datetime', label: 'Expiration Date' },
    { key: 'description', label: 'Reminder' },
    { key: 'itemID', label: 'Item ID' },
    { key: 'itemName', label: 'Item Name' },
    { key: 'brand', label: 'Brand' },
    { key: 'unit', label: 'Unit' },
    { key: 'amount', label: 'Amount', inputType: 'number' },
    {
      key: 'completed',
      label: 'Done',
      render: (value) => (value ? '✔️' : ''),
    },
    {
      key: 'actions',
      label: 'Actions',
      isAction: true,
    },
  ];

  const visibleReminders = reminders.filter((r) => showCompleted || !r.completed);

  return (
    <ScrollView style={{ backgroundColor: colors.background, flex: 1 }} contentContainerStyle={{ padding: 20 }}>
      <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 12 }}>
        <Switch
          value={showCompleted}
          onValueChange={setShowCompleted}
          thumbColor={colors.primary}
        />
        <Button onPress={() => setShowCompleted(!showCompleted)}>
          {showCompleted ? 'Hide Completed' : 'Show Completed'}
        </Button>
      </View>

      <GenericTable<Reminder>
        data={visibleReminders}
        columns={columns}
        onEdit={() => {}}
        onDelete={handleDelete}
        onEditToggle={handleComplete}
      />
    </ScrollView>
  );
};

export default ReminderScreen;
