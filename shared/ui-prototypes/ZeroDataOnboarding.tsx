import React, { useState } from 'react';
import { View, Text, Switch, TouchableOpacity, StyleSheet } from 'react-native';

/**
 * JUKWA Zero-Data Onboarding Component
 * Source: pasted_text_d333bebb...txt
 */

export default function ZeroDataOnboarding() {
  const [incognitoMode, setIncognitoMode] = useState(true);

  return (
    <View style={styles.container}>
      <Text style={styles.headerTitle}>Welcome to Jukwa.</Text>
      
      <View style={styles.guaranteeBox}>
        <Text style={styles.guaranteeHeader}>THE ZERO-DATA GUARANTEE</Text>
        <Text style={styles.guaranteeText}>
          We do not know who you are. We do not want to know who you are. 
          Jukwa operates on a strict 0-Data Collection Policy. No names, no phone numbers, 
          no tracking. Your voice is your power; your anonymity is your shield.
        </Text>
      </View>

      <View style={styles.choiceContainer}>
        <View style={styles.choiceRow}>
          <Text style={styles.choiceTitle}>Strict Incognito Mode</Text>
          <Switch 
            value={incognitoMode} 
            onValueChange={setIncognitoMode} 
            trackColor={{ true: '#00FF00', false: '#767577' }}
          />
        </View>
        <Text style={styles.choiceDescription}>
          All uploads are routed through our independent 'Citizen Vault' relay. 
          Device metadata is destroyed on your phone before sending. 
          You are completely untraceable.
        </Text>
      </View>

      <TouchableOpacity style={styles.enterButton}>
        <Text style={styles.enterButtonText}>ENTER THE PLATFORM</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
    padding: 24,
    justifyContent: 'center'
  },
  headerTitle: {
    color: '#00FF00',
    fontSize: 32,
    fontWeight: 'bold',
    marginBottom: 40
  },
  guaranteeBox: {
    borderWidth: 1,
    borderColor: '#00FF00',
    padding: 20,
    marginBottom: 40
  },
  guaranteeHeader: {
    color: '#00FF00',
    fontWeight: 'bold',
    marginBottom: 10
  },
  guaranteeText: {
    color: '#FFF',
    lineHeight: 20
  },
  choiceContainer: {
    marginBottom: 40
  },
  choiceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10
  },
  choiceTitle: {
    color: '#FFF',
    fontSize: 18,
    fontWeight: 'bold'
  },
  choiceDescription: {
    color: '#888',
    fontSize: 14
  },
  enterButton: {
    backgroundColor: '#00FF00',
    padding: 16,
    alignItems: 'center',
    borderRadius: 4
  },
  enterButtonText: {
    color: '#000',
    fontWeight: 'bold',
    fontSize: 16
  }
});
