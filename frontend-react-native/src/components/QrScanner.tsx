import { CameraView, useCameraPermissions } from 'expo-camera';
import { useState } from 'react';
import { Platform, Text, TextInput, TouchableOpacity, View } from 'react-native';

type Props = {
  disabled?: boolean;
  onScan: (token: string) => void;
};

export function QrScanner({ disabled, onScan }: Props) {
  const [permission, requestPermission] = useCameraPermissions();
  const [paste, setPaste] = useState('');
  const [scanned, setScanned] = useState(false);

  if (Platform.OS === 'web') {
    return <PasteToken value={paste} onChange={setPaste} disabled={disabled} onConfirm={onScan} />;
  }

  if (!permission) {
    return <Text className="text-so2-textMuted">Verificando permissão da câmera…</Text>;
  }

  if (!permission.granted) {
    return (
      <View className="gap-y-3">
        <Text className="text-sm text-so2-textMuted">
          Sem câmera, cole o token do QR divulgado no evento.
        </Text>
        <TouchableOpacity
          className="items-center rounded-md border border-so2-primary py-3"
          onPress={() => void requestPermission()}
        >
          <Text className="font-semibold text-so2-primary">Permitir câmera</Text>
        </TouchableOpacity>
        <PasteToken value={paste} onChange={setPaste} disabled={disabled} onConfirm={onScan} />
      </View>
    );
  }

  return (
    <View className="gap-y-3">
      <View className="h-56 overflow-hidden rounded-lg bg-black">
        <CameraView
          style={{ flex: 1 }}
          facing="back"
          barcodeScannerSettings={{ barcodeTypes: ['qr'] }}
          onBarcodeScanned={({ data }) => {
            if (scanned || disabled) {
              return;
            }
            setScanned(true);
            onScan(data.trim());
          }}
        />
      </View>
      <TouchableOpacity onPress={() => setScanned(false)}>
        <Text className="text-center text-so2-primary">Ler outro QR</Text>
      </TouchableOpacity>
      <PasteToken value={paste} onChange={setPaste} disabled={disabled} onConfirm={onScan} />
    </View>
  );
}

function PasteToken({
  value,
  onChange,
  disabled,
  onConfirm,
}: {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  onConfirm: (token: string) => void;
}) {
  return (
    <View>
      <Text className="mb-1 text-sm text-so2-textMuted">Ou cole o token do QR</Text>
      <TextInput
        className="mb-2 rounded-md border border-so2-border px-4 py-3 text-so2-text"
        value={value}
        onChangeText={onChange}
        editable={!disabled}
        autoCapitalize="none"
        autoCorrect={false}
        placeholder="token"
        placeholderTextColor="#9CA3AF"
        accessibilityLabel="Token QR de presença"
      />
      <TouchableOpacity
        className="items-center rounded-md bg-so2-primary py-3"
        disabled={disabled || !value.trim()}
        onPress={() => onConfirm(value.trim())}
      >
        <Text className="font-semibold text-white">Usar token</Text>
      </TouchableOpacity>
    </View>
  );
}
