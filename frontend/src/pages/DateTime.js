import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import Calendar from './../components/common/Calendar/Calendar';
import TimePicker from './../components/Filters/TimePicker';
import SubmitButton from './../components/common/Buttons/SubmitButton';
import BackButton from './../components/common/Buttons/BackButton';

export default class DateTime extends React.Component {
  constructor(props) {
    super(props)
  }

  setDate = (dataFromChild) => {
    this.props.setDateFromParent(dataFromChild)
  }

  setStartTime = (dataFromChild) => {
    this.props.setStartTimeFromParent(dataFromChild)
  }

  setEndTime = (dataFromChild) => {
    this.props.setEndTimeFromParent(dataFromChild)
  }

  render(){
    return(
      <View style={styles.container}>
        <Calendar setDateFromParent={this.setDate}/>
        <TimePicker
          setStartTimeFromParent={this.setStartTime}
          setEndTimeFromParent={this.setEndTime}
        />
        <SubmitButton continue={this.props.continue}/>
        <BackButton previous={this.props.previous}/>
      </View>
    );
  }
}

let styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
