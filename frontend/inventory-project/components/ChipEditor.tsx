import React, { useState } from 'react';
import { View, StyleSheet, Keyboard } from 'react-native';
import { Chip, TextInput, useTheme } from 'react-native-paper';

interface ChipEditorProps {
  categories: string[];
  onChange: (updated: string[]) => void;
}

const ChipEditor: React.FC<ChipEditorProps> = ({ categories, onChange }) => {
  const [input, setInput] = useState('');
  const { colors } = useTheme();

  const handleAdd = () => {
    const trimmed = input.trim();
    if (trimmed && !categories.includes(trimmed)) {
      onChange([...categories, trimmed]);
      setInput('');
      Keyboard.dismiss();
    }
  };

  const handleRemove = (name: string) => {
    onChange(categories.filter((c) => c !== name));
  };

  return (
    <View style={styles.container}>
      {categories.map((category) => (
        <Chip
          key={category}
          onClose={() => handleRemove(category)}
          mode="outlined"
          style={styles.chip}
        >
          {category}
        </Chip>
      ))}
      <TextInput
        value={input}
        onChangeText={setInput}
        onSubmitEditing={handleAdd}
        placeholder="Add category"
        mode="outlined"
        dense
        style={styles.input}
        placeholderTextColor={colors.onSurfaceVariant}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'flex-start',
    paddingTop: 4,
    gap: 4,
    flex: 5,
  },
  chip: {
    margin: 2,
  },
  input: {
    minWidth: 100,
    maxWidth: 140,
    padding: 4,
    margin: 2,
  },
});

export default ChipEditor;
