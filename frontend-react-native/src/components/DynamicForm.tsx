import { Switch, Text, TextInput, TouchableOpacity, View } from 'react-native';
import type { FormField } from '../lib/formSchema';

type Props = {
  fields: FormField[];
  values: Record<string, unknown>;
  errors: Record<string, string>;
  disabled?: boolean;
  onChange: (name: string, value: unknown) => void;
};

export function DynamicForm({ fields, values, errors, disabled, onChange }: Props) {
  if (fields.length === 0) {
    return <Text className="text-so2-textMuted">Este tipo não define campos no formulário.</Text>;
  }
  return (
    <View className="gap-y-4">
      {fields.map((field) => {
        const error = errors[field.name];
        const value = values[field.name];
        return (
          <View key={field.name}>
            <Text className="mb-1 text-sm font-medium text-so2-textMuted">
              {field.title}
              {field.required ? ' *' : ''}
            </Text>
            {field.enumValues ? (
              <View className="gap-y-2">
                {field.enumValues.map((option) => {
                  const selected = String(value ?? '') === String(option);
                  return (
                    <TouchableOpacity
                      key={String(option)}
                      disabled={disabled}
                      onPress={() => onChange(field.name, option)}
                      className={`rounded-md border px-4 py-3 ${selected ? 'border-so2-primary bg-blue-50' : 'border-so2-border bg-white'}`}
                    >
                      <Text className="text-so2-text">{String(option)}</Text>
                    </TouchableOpacity>
                  );
                })}
              </View>
            ) : field.type === 'boolean' ? (
              <Switch
                disabled={disabled}
                value={Boolean(value)}
                onValueChange={(next) => onChange(field.name, next)}
                accessibilityLabel={field.title}
              />
            ) : (
              <TextInput
                className={`rounded-md border px-4 py-3 text-so2-text ${error ? 'border-red-400' : 'border-so2-border'}`}
                editable={!disabled}
                value={value == null ? '' : String(value)}
                onChangeText={(text) =>
                  onChange(
                    field.name,
                    field.type === 'number' || field.type === 'integer'
                      ? text === ''
                        ? ''
                        : Number(text)
                      : text,
                  )
                }
                multiline={(field.maxLength ?? 0) > 180}
                keyboardType={field.type === 'number' || field.type === 'integer' ? 'numeric' : 'default'}
                placeholderTextColor="#9CA3AF"
                accessibilityLabel={field.title}
              />
            )}
            {field.description ? <Text className="mt-1 text-xs text-so2-textMuted">{field.description}</Text> : null}
            {error ? (
              <Text className="mt-1 text-sm text-so2-dangerText" accessibilityLiveRegion="polite">
                {error}
              </Text>
            ) : null}
          </View>
        );
      })}
    </View>
  );
}
